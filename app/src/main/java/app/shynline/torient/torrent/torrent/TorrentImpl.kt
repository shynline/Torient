package app.shynline.torient.torrent.torrent

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import app.shynline.torient.common.downloadDir
import app.shynline.torient.common.observable.Observable
import app.shynline.torient.common.torrentDir
import app.shynline.torient.model.TorrentDetail
import app.shynline.torient.model.TorrentIdentifier
import app.shynline.torient.torrent.service.TorientService
import app.shynline.torient.torrent.states.ManageState
import com.frostwire.jlibtorrent.SessionManager
import com.frostwire.jlibtorrent.Sha1Hash
import com.frostwire.jlibtorrent.TorrentHandle
import com.frostwire.jlibtorrent.TorrentInfo
import kotlinx.coroutines.*
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.util.*
import kotlin.concurrent.fixedRateTimer

class TorrentImpl(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher
) : BaseTorrent(),
    ServiceConnection,
    TorrentController,
    Observable<Torrent.Listener> {
    private val session: SessionManager = SessionManager(false)
    private var isActivityRunning = false
    private var service: TorientService? = null
    private val intent: Intent = Intent(context, TorientService::class.java)

    private val torrentsDetails: MutableMap<String, TorrentDetail> = hashMapOf()

    private var periodicTimer: Timer? = null

    private fun periodicTask() = torrentScope.launch {
        if (isActivityRunning) {
            requestTorrentStats(managedTorrents.keys.toList())
        } else {
            service?.updateNotification(
                managedTorrents.size,
                session.downloadRate(),
                session.uploadRate()
            )
        }
    }

    init {
        start()
    }

    override fun onActivityStart() {
        isActivityRunning = true
        handleServiceState()
    }

    override fun onActivityStop() {
        isActivityRunning = false
        handleServiceState()
    }

    override fun start() {
        super.start()
        session.addListener(this)
        session.start()
        periodicTimer = fixedRateTimer(
            name = "periodicTaskTorrentsList",
            initialDelay = 1000,
            period = 1000
        ) { periodicTask() }
    }

    override fun stop() {
        periodicTimer?.cancel()
        super.stop()
        session.stop() //blocking
        session.removeListener(this@TorrentImpl)
    }

    private fun handleServiceState() = GlobalScope.launch {
        if (isActivityRunning) {
            if (service != null) {
                service!!.background()
            } else {
                if (!session.isRunning) {
                    start()
                }
            }
        } else {
            if (managedTorrents.isEmpty()) {
                stop()
                unbindService()
                context.stopService(intent)
            } else {
                if (service != null) {
                    service!!.foreground(
                        managedTorrents.size,
                        session.downloadRate(),
                        session.uploadRate()
                    )
                } else {
                    bindService()
                    context.startService(intent)
                }
            }
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        service = null
    }

    private suspend fun requestTorrentStats(torrents: List<String>) {
        torrents.forEach { infoHash ->
            session.find(Sha1Hash(infoHash))?.let { handle ->
                handleTorrentProgress(handle)
            }
        }
    }

    override fun findHandle(sha1: Sha1Hash): TorrentHandle? {
        session.find(sha1)?.let {
            if (it.isValid)
                return it
        }
        return null
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        this.service = (service as? TorientService.TorientBinder)?.service
        handleServiceState()
    }

    private fun unbindService() {
        if (service != null) {
            context.unbindService(this)
            service = null
        }
    }

    private fun bindService() {
        if (service == null) {
            context.bindService(intent, this, Context.BIND_AUTO_CREATE)
        }
    }

    ///////////////////////////////////////////////////////////////
    // Torrent interface impl

    /**
     * Add a torrent to service via magnet
     * it requires a valid magnet
     *
     * @param identifier
     */
    override suspend fun addTorrent(identifier: TorrentIdentifier) {
        // Return if the torrent is already being managed by session
        if (managedTorrents.containsKey(identifier.infoHash))
            return
        managedTorrents[identifier.infoHash] = ManageState.UNKNOWN
        readTorrentFileFromCache(identifier.infoHash)?.let {
            session.download(getTorrentInfo(it), context.downloadDir)
            return
        }
        session.download(identifier.magnet, context.downloadDir)
    }


    /**
     * Get the torrentDetail from identifier
     * It tries to retrieve from cache first
     * and from magnet as the last resort
     *
     * @param identifier
     * @return TorrentDetail or null
     */
    override suspend fun getTorrentDetail(identifier: TorrentIdentifier): TorrentDetail? =
        withContext(ioDispatcher) {
            getTorrentDetailFromInfoHash(identifier.infoHash)?.let {
                return@withContext it
            }
            return@withContext getTorrentDetail(identifier.magnet)
        }

    /**
     * This method tries to get the torrent detail from infoHash
     * it could be a cached torrentDetail
     * or loading it from a persisted cache as torrent file
     *
     * @param infoHash
     * @return TorrentDetail or null
     */
    override suspend fun getTorrentDetailFromInfoHash(infoHash: String): TorrentDetail? {
        // Check if we already have it in our cache
        if (torrentsDetails.containsKey(infoHash)) {
            return torrentsDetails[infoHash]
        }
        return getTorrentDetail(readTorrentFileFromCache(infoHash))
    }

    private fun readTorrentFileFromCache(infoHash: String): ByteArray? {
        // Load torrent from file directory if exists
        val file = File(context.torrentDir, "$infoHash.torrent")
        if (file.exists()) {
            // If the torrent file exists we read it
            // and parse it
            BufferedInputStream(file.inputStream()).use {
                return it.readBytes()
            }
        }
        return null
    }

    /**
     * This method inquiry the torrent file from its magnet
     * TODO: It's not working
     * @param magnet
     * @return TorrentDetail or null
     */
    override suspend fun getTorrentDetail(magnet: String): TorrentDetail? =
        withContext(ioDispatcher) {
            // Waiting for at most 10 seconds to find at least 10 dht nodes if doesn't exist
            var times = 0
            while (dhtNodes < 10 && times < 100) {
                delay(1000)
                times += 1
            }
            val bytes: ByteArray? = session.fetchMagnet(magnet, 30)
            return@withContext getTorrentDetail(bytes)
        }

    private suspend fun getTorrentInfo(data: ByteArray): TorrentInfo {
        return TorrentInfo(data)
    }

    /**
     * Parse a torrent file data to a TorrentDetail
     *
     * @param data
     * @return TorrentDetail or null
     */
    override suspend fun getTorrentDetail(data: ByteArray?): TorrentDetail? =
        withContext(ioDispatcher) {
            if (data == null)
                return@withContext null
            // Decode the byteArray
            val torrentInfo = getTorrentInfo(data)
            // Create the torrentDetail
            val torrentDetail = TorrentDetail.from(torrentInfo)
            torrentDetail.serviceState =
                managedTorrents[torrentDetail.infoHash] ?: ManageState.UNKNOWN
            // Cache it in memory
            torrentsDetails[torrentDetail.infoHash] = torrentDetail

            // Save torrent file here if it doesn't exist
            val file = File(context.torrentDir, "${torrentDetail.infoHash}.torrent")
            if (!file.exists()) {
                try {
                    // Simply create and write it to file
                    @Suppress("BlockingMethodInNonBlockingContext")
                    file.createNewFile()
                    BufferedOutputStream(file.outputStream()).use {
                        it.write(data)
                    }
                } catch (e: Exception) {
                    // It's a case if we could not create a persisted
                    // Which is not a big deal I guess
                }
            }
            return@withContext torrentDetail
        }


    /**
     * Remove a torrent from service and cache
     *
     * @param infoHash
     * @return true if there is any torrent to be removed false otherwise
     */
    override suspend fun removeTorrent(infoHash: String): Boolean {
        managedTorrents.remove(infoHash)
        session.find(Sha1Hash(infoHash))?.let {
            session.remove(it)
            return true
        }
        return false
    }

}