package app.shynline.torient.torrent.torrent

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import app.shynline.torient.common.downloadDir
import app.shynline.torient.common.observable.Observable
import app.shynline.torient.common.torrentDir
import app.shynline.torient.database.datasource.torrent.InternalTorrentDataSource
import app.shynline.torient.database.states.TorrentUserState
import app.shynline.torient.model.TorrentIdentifier
import app.shynline.torient.model.TorrentModel
import app.shynline.torient.model.TorrentOverview
import app.shynline.torient.torrent.service.TorientService
import com.frostwire.jlibtorrent.*
import kotlinx.coroutines.*
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.util.*
import kotlin.concurrent.fixedRateTimer

class TorrentImpl(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher,
    override val internalTorrentDataSource: InternalTorrentDataSource
) : BaseTorrent(internalTorrentDataSource),
    ServiceConnection,
    TorrentController,
    Observable<Torrent.Listener> {
    private val session: SessionManager = SessionManager(false)
    private var isActivityRunning = false
    private val sessionParams: SessionParams = SessionParams(
        SettingsPack()
            .enableDht(true)
            .activeDownloads(5)
            .activeSeeds(5)
            .connectionsLimit(50)
    )
    private var service: TorientService? = null
    private val intent: Intent = Intent(context, TorientService::class.java)

    private val torrentModels: MutableMap<String, TorrentModel> = hashMapOf()

    private var periodicTimer: Timer? = null

    private fun periodicTask() = torrentScope.launch {
        requestTorrentStats()
        if (!isActivityRunning) {
            service?.updateNotification(
                managedTorrents.size,
                session.stats().downloadRate(),
                session.stats().uploadRate()
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
        session.start(sessionParams)
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

    private fun requestTorrentStats() {
        GlobalScope.launch {
            managedTorrents.forEach {
                session.find(Sha1Hash(it.key))?.let { handle ->
                    if (handle.isValid)
                        handleTorrentProgress(handle)
                }
            }
        }
    }

    override suspend fun getTorrentOverview(infoHash: String): TorrentOverview? {
        session.find(Sha1Hash(infoHash))?.let { handle ->
            if (handle.isValid) {
                val state = handle.status()
                val info = handle.torrentFile() ?: null
                return TorrentOverview(
                    name = handle.name(),
                    infoHash = infoHash,
                    progress = state.progress(),
                    numPiece = info?.numPieces() ?: 0,
                    pieceLength = info?.pieceLength() ?: 0,
                    size = info?.totalSize() ?: 0,
                    userState = TorrentUserState.ACTIVE,
                    creator = info?.creator() ?: "",
                    comment = info?.comment() ?: "",
                    createdDate = (info?.creationDate() ?: 0) * 1000,
                    private = info?.isPrivate ?: false,
                    lastSeenComplete = state.lastSeenComplete()
                )
            }
        }
        readTorrentFileFromCache(infoHash)?.let {
            val info = TorrentInfo(it)
            return TorrentOverview(
                name = info.name(),
                infoHash = infoHash,
                progress = 0f,
                numPiece = info.numPieces(),
                pieceLength = info.pieceLength(),
                size = info.totalSize(),
                userState = TorrentUserState.PAUSED,
                creator = info.creator(),
                comment = info.comment(),
                createdDate = info.creationDate() * 1000,
                private = info.isPrivate,
                lastSeenComplete = 0
            )
        }

        return null
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
        managedTorrents[identifier.infoHash] = null
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
    override suspend fun getTorrentModel(identifier: TorrentIdentifier): TorrentModel? =
        withContext(ioDispatcher) {
            getTorrentModelFromInfoHash(identifier.infoHash)?.let {
                return@withContext it
            }
            return@withContext getTorrentModel(identifier.magnet)
        }

    /**
     * This method tries to get the torrent detail from infoHash
     * it could be a cached torrentDetail
     * or loading it from a persisted cache as torrent file
     *
     * @param infoHash
     * @return TorrentDetail or null
     */
    override suspend fun getTorrentModelFromInfoHash(infoHash: String): TorrentModel? {
        // Check if we already have it in our cache
        if (torrentModels.containsKey(infoHash)) {
            return torrentModels[infoHash]
        }
        return getTorrentModel(readTorrentFileFromCache(infoHash))
    }

    private fun readTorrentFileFromCache(infoHash: String): ByteArray? {
        // Load torrent from file directory if exists
        val file = File(context.torrentDir, "${infoHash.toLowerCase(Locale.ROOT)}.torrent")
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
     * @param magnet
     * @return TorrentDetail or null
     */
    override suspend fun getTorrentModel(magnet: String): TorrentModel? =
        withContext(ioDispatcher) {
            // Waiting for at most 10 seconds to find at least 10 dht nodes if doesn't exist
            var times = 0

            while (session.stats().dhtNodes() < 10 && times < 100) {
                delay(100)
                times += 1
            }
            val bytes: ByteArray? = session.fetchMagnet(magnet, 30)
            return@withContext getTorrentModel(bytes)
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
    override suspend fun getTorrentModel(data: ByteArray?): TorrentModel? =
        withContext(ioDispatcher) {
            if (data == null)
                return@withContext null
            val torrentInfo = try {
                // Decode the byteArray
                getTorrentInfo(data)
            } catch (e: Exception) {
                return@withContext null
            }
            // Create the torrentDetail
            val torrentDetail = TorrentModel.from(torrentInfo)
            // Cache it in memory
            torrentModels[torrentDetail.infoHash] = torrentDetail

            // Save torrent file here if it doesn't exist
            saveTorrentFileToCache(torrentDetail.infoHash, data)
            return@withContext torrentDetail
        }

    override fun saveTorrentFileToCache(infoHash: String, data: ByteArray) {
        val file = File(context.torrentDir, "${infoHash.toLowerCase(Locale.ROOT)}.torrent")
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

    override suspend fun removeTorrentFiles(name: String): Boolean {
        return removeTorrentFile(File(context.downloadDir, name))
    }

    private suspend fun removeTorrentFile(file: File): Boolean {
        if (file.exists()) {
            if (file.isDirectory) {
                file.listFiles()?.forEach {
                    removeTorrentFile(it)
                }
            }
            return file.delete()
        }
        return false
    }


    override fun isTorrentFileCached(infoHash: String): Boolean {
        return File(context.torrentDir, "${infoHash.toLowerCase(Locale.ROOT)}.torrent")
            .exists()
    }

}