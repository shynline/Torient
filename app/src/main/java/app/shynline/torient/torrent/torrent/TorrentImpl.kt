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
import com.frostwire.jlibtorrent.SessionManager
import com.frostwire.jlibtorrent.TorrentInfo
import kotlinx.coroutines.*
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File

class TorrentImpl(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher
) : BaseTorrent(),
    ServiceConnection,
    TorrentController,
    Observable<Torrent.Listener> {
    private val session: SessionManager = SessionManager()
    private var isActivityRunning = false
    private var service: TorientService? = null
    private val intent: Intent = Intent(context, TorientService::class.java)

    private val torrentsDetails: MutableMap<String, TorrentDetail> = hashMapOf()

    init {
        session.addListener(this)
        session.start()
    }

    override fun onActivityStart() {
        isActivityRunning = true
        handleServiceState()
    }

    override fun onActivityStop() {
        isActivityRunning = false
        handleServiceState()
    }

    private fun handleServiceState() = GlobalScope.launch {
        if (isActivityRunning) {
            if (service != null) {
                service!!.background()
            } else {
                if (!session.isRunning) {
                    session.start()
                }
            }
        } else {
            if (torrentsHandles.isEmpty()) {
                session.stop() //blocking
                session.removeListener(this@TorrentImpl)
                unbindService()
                context.stopService(intent)
            } else {
                if (service != null) {
                    service!!.foreground()
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
        managedTorrents[identifier.infoHash] = ManageState.UNKNOWN
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
        // Load torrent from file directory if exists
        val file = File(context.torrentDir, "$infoHash.torrent")
        if (file.exists()) {
            // If the torrent file exists we read it
            // and parse it
            BufferedInputStream(file.inputStream()).use {
                return getTorrentDetail(it.readBytes())
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
            val torrentInfo = TorrentInfo(data)
            // Create the torrentDetail
            val torrentDetail = TorrentDetail.from(torrentInfo)
            torrentDetail.serviceState = managedTorrents[torrentDetail.infoHash]
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
     * Send a request to resume downloading
     *
     * @param infoHash
     * @return true if there is a valid handler and false otherwise
     */
    override suspend fun resumeTorrent(infoHash: String): Boolean {
        if (ensureHandlerIsValid(infoHash)) {
            torrentsHandles[infoHash]!!.resume()
            return true
        }
        return false
    }

    /**
     * Handlers might get to invalid state
     * before any request view handlers we have to make sure the handler is valid
     * this method automatically remove the old handler and
     * tries to add the torrent again to retrieve a new one
     *
     * @param infoHash
     * @return true if handler is valid and false if not
     */
    private suspend fun ensureHandlerIsValid(infoHash: String): Boolean {
        if (torrentsHandles.containsKey(infoHash)) {
            // Handler exists
            if (torrentsHandles[infoHash]!!.isValid) {
                return true
            } else {
                managedTorrents.remove(infoHash)
                // Remove torrent from the service
                session.remove(torrentsHandles[infoHash])
                // Remove it from our cache
                torrentsHandles.remove(infoHash)
                // Retrieve the torrent detail with info hash
                getTorrentDetailFromInfoHash(infoHash)?.let {
                    // We add the torrent but getting handler is asynchronous so
                    // we still have to return false
                    addTorrent(it.toIdentifier())
                }
                return false
            }
        }
        // There is no handler we assume it's invalid
        return false
    }

    /**
     * Send a request to pause a torrent
     *
     * @param infoHash
     * @return true if handler is valid and request has been made false otherwise
     */
    override suspend fun pauseTorrent(infoHash: String): Boolean {
        if (ensureHandlerIsValid(infoHash)) {
            torrentsHandles[infoHash]!!.pause()
            return true
        }
        return false
    }


    /**
     * Remove a torrent from service and cache
     *
     * @param infoHash
     * @return true if there is any torrent to be removed false otherwise
     */
    override suspend fun removeTorrent(infoHash: String): Boolean {
        managedTorrents.remove(infoHash)
        if (torrentsHandles.containsKey(infoHash)) {
            session.remove(torrentsHandles[infoHash]!!)
            torrentsHandles.remove(infoHash)
            return true
        }
        return false
    }

}