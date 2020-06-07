package app.shynline.torient.domain.torrentmanager.torrent

import android.content.Context
import app.shynline.torient.common.logTorrent
import app.shynline.torient.domain.database.common.states.TorrentUserState
import app.shynline.torient.domain.database.datasource.torrent.InternalTorrentDataSource
import app.shynline.torient.domain.database.datasource.torrentpreference.TorrentPreferenceDataSource
import app.shynline.torient.domain.mediator.usecases.GetFilePriorityUseCase
import app.shynline.torient.domain.models.*
import app.shynline.torient.domain.torrentmanager.common.events.TorrentMetaDataEvent
import app.shynline.torient.domain.torrentmanager.common.events.TorrentProgressEvent
import app.shynline.torient.domain.torrentmanager.common.states.TorrentDownloadingState
import app.shynline.torient.domain.torrentmanager.service.SessionController
import app.shynline.torient.domain.userpreference.UserPreference
import app.shynline.torient.utils.downloadDir
import app.shynline.torient.utils.observable.BaseObservable
import app.shynline.torient.utils.torrentDir
import com.frostwire.jlibtorrent.Priority
import com.frostwire.jlibtorrent.TorrentHandle
import com.frostwire.jlibtorrent.TorrentInfo
import com.frostwire.jlibtorrent.TorrentStatus
import com.frostwire.jlibtorrent.alerts.AddTorrentAlert
import com.frostwire.jlibtorrent.alerts.MetadataReceivedAlert
import kotlinx.coroutines.*
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.util.*

class TorrentImpl(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher,
    private val userPreference: UserPreference,
    private val sessionController: SessionController,
    private val torrentPreferenceDataSource: TorrentPreferenceDataSource,
    private val internalTorrentDataSource: InternalTorrentDataSource,
    private val getFilePriorityUseCase: GetFilePriorityUseCase
) : BaseObservable<Torrent.Listener>(),
    SessionController.SessionControllerInterface, Torrent {

    private val torrentModels: MutableMap<String, TorrentModel> = hashMapOf()
    private lateinit var torrentScope: CoroutineScope

    init {
        sessionController.setInterface(this)
    }

    override fun onStart() {
        torrentScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    override fun onStop() {
        torrentScope.cancel()
    }

    override fun periodic() {
        periodicTask()
    }

    private fun periodicTask() = torrentScope.launch {
        requestTorrentStats()
    }

    private suspend fun handleTorrentProgress(handle: TorrentHandle) {
        val status = handle.status()
        val infoHash = handle.infoHash().toHex()
        handleTorrentState(infoHash, handle, status, status.state())?.let { event ->
            if (event.state != TorrentDownloadingState.UNKNOWN) {
                sessionController.setManageTorrentState(infoHash, status.state())
                getListeners().forEach { listener ->
                    listener.onStatReceived(event)
                }
            }
        }
    }


    private suspend fun handleTorrentState(
        infoHash: String,
        handle: TorrentHandle,
        status: TorrentStatus,
        state: TorrentStatus.State?
    ): TorrentProgressEvent? =
        when (state) {
            TorrentStatus.State.CHECKING_FILES -> {
                TorrentProgressEvent.checkingFileEvent(infoHash, status.progress())
            }
            TorrentStatus.State.DOWNLOADING_METADATA -> {
                TorrentProgressEvent.downloadingMetaDataEvent(infoHash)
            }
            TorrentStatus.State.DOWNLOADING -> {
                val fileProgress = handle.fileProgress()
                val progress = status.progress()
                internalTorrentDataSource.setTorrentProgress(
                    infoHash, progress, lastSeenComplete = status.lastSeenComplete(),
                    fileProgress = fileProgress
                )
                dispatchStatsIfNecessary(infoHash, handle.torrentFile())
                TorrentProgressEvent.downloadingEvent(
                    infoHash, progress, status.downloadRate(), status.uploadRate(),
                    status.listPeers(), status.numPeers(), fileProgress.toList()
                )
            }
            TorrentStatus.State.FINISHED -> {
                internalTorrentDataSource.setTorrentFinished(
                    infoHash, true, fileProgress = handle.fileProgress()
                )
                // This is a rare case when the file is so small
                // Also it happens when whole downloading process being done
                // in background process
                dispatchStatsIfNecessary(infoHash, handle.torrentFile())
                TorrentProgressEvent.finishedEvent(infoHash)
            }
            TorrentStatus.State.SEEDING -> {
                internalTorrentDataSource.setTorrentFinished(
                    infoHash, true, fileProgress = handle.fileProgress()
                )
                // This is a rare case when the file is so small
                // Also it happens when whole downloading process being done
                // in background process
                dispatchStatsIfNecessary(infoHash, handle.torrentFile())
                TorrentProgressEvent.seedingEvent(
                    infoHash, status.downloadRate(), status.uploadRate(),
                    status.listPeers(), status.numPeers()
                )
            }
            TorrentStatus.State.ALLOCATING -> TorrentProgressEvent.allocatingEvent(infoHash)
            TorrentStatus.State.CHECKING_RESUME_DATA -> {
                TorrentProgressEvent.checkingResumeDateEvent(infoHash)
            }
            TorrentStatus.State.UNKNOWN -> TorrentProgressEvent.unknownEvent(infoHash)
            null -> null
        }

    override fun onAlertAddTorrentAlert(addTorrentAlert: AddTorrentAlert) {
        val infoHash = addTorrentAlert.handle().infoHash().toHex()
        if (addTorrentAlert.error().isError) {
            // TODO this case have to be handled
            logTorrent(
                "onAlertAddTorrentAlert Error: ${addTorrentAlert.error().message()}",
                infoHash
            )
            return
        }
        val handle = sessionController.findHandle(infoHash) ?: return
        logTorrent("onAlertAddTorrentAlert resumed", infoHash)
        handle.resume()
        torrentScope.launch {
            applyPreference(handle, infoHash)
            handle.torrentFile()?.let { torrentInfo ->
                getFilePriorityUseCase(GetFilePriorityUseCase.In(torrentInfo)).filePriority?.let { fp ->
                    setFilesPriority(infoHash, fp)
                }
            }
        }
    }

    override fun onAlertMetaDataReceived(metadataReceivedAlert: MetadataReceivedAlert) {
        val torrentInfo = TorrentInfo(metadataReceivedAlert.torrentData())
        val infoHash = torrentInfo.infoHash().toHex()
        logTorrent("onAlertMetaDataReceived", infoHash)
        torrentScope.launch {
            getFilePriorityUseCase(GetFilePriorityUseCase.In(torrentInfo)).filePriority?.let { fp ->
                setFilesPriority(infoHash, fp)
            }
            // Cache it in torrent storage
            saveTorrentFileToCache(infoHash, torrentInfo.bencode())
        }
    }

    private fun dispatchStatsIfNecessary(infoHash: String, torrentInfo: TorrentInfo) {
        if (sessionController.getManageTorrentState(infoHash) == TorrentStatus.State.DOWNLOADING_METADATA) {
            val metaDataEvent = TorrentMetaDataEvent(
                infoHash,
                TorrentModel.from(torrentInfo)
            )
            getListeners().forEach {
                it.onStatReceived(metaDataEvent)
            }
        }
    }


    override fun updateTorrentPreference(infoHash: String) {
        applyPreference(infoHash)
    }

    override fun onUpdateGlobalPreference() {
        applyPreference(null)
    }

    private fun applyPreference(infoHash: String?) = torrentScope.launch {
        // If infoHash is null all managed torrents will be updated
        val torrents =
            if (infoHash != null) listOf(infoHash) else sessionController.getAllManagedTorrent()
        torrents.forEach { ih ->
            sessionController.findHandle(ih)?.let { handle ->
                if (handle.isValid) {
                    applyPreference(handle, ih)
                }
            }
        }
    }

    private suspend fun applyPreference(handle: TorrentHandle, infoHash: String) {
        val torrentPreference = torrentPreferenceDataSource.getTorrentPreference(infoHash)
        if (torrentPreference.honorGlobalRate) {
            handle.uploadLimit =
                if (userPreference.globalUploadRateLimit) userPreference.globalUploadRate * 1024 else 0
            handle.downloadLimit =
                if (userPreference.globalDownloadRateLimit) userPreference.globalDownloadRate * 1024 else 0
        } else {
            handle.uploadLimit =
                if (torrentPreference.uploadRateLimit) torrentPreference.uploadRate * 1024 else 0
            handle.downloadLimit =
                if (torrentPreference.downloadRateLimit) torrentPreference.downloadRate * 1024 else 0
        }
        sessionController.applyPreference(
            SessionController.PreferenceParams(
                maxConnection = userPreference.globalMaxConnection
            )
        )
    }

    override suspend fun getAllManagedTorrents(): List<String> {
        return sessionController.getAllManagedTorrent()
    }

    override suspend fun setFilePriority(
        infoHash: String,
        index: Int,
        torrentFilePriority: TorrentFilePriority,
        torrentHandle: TorrentHandle?
    ) {
        var handle = torrentHandle
        if (handle == null) {
            handle = sessionController.findHandle(infoHash)
        }
        if (handle != null) {
            if (handle.isValid) {
                val p = if (!torrentFilePriority.active) {
                    Priority.IGNORE
                } else {
                    when (torrentFilePriority.priority) {
                        FilePriority.NORMAL -> Priority.FOUR
                        FilePriority.HIGH -> Priority.SIX
                        FilePriority.LOW -> Priority.NORMAL
                        FilePriority.MIXED -> {
                            throw IllegalStateException("Files can not have mixed priority.")
                        }
                    }
                }
                handle.filePriority(index, p)
            }
        }
    }


    override suspend fun setFilesPriority(
        infoHash: String,
        torrentFilePriorities: List<TorrentFilePriority>
    ) {
        sessionController.findHandle(infoHash)?.let { handle ->
            if (handle.isValid) {
                torrentFilePriorities.forEachIndexed { index, torrentFilePriority ->
                    setFilePriority(infoHash, index, torrentFilePriority, handle)
                }
            }
        }
    }

    private fun requestTorrentStats() {
        torrentScope.launch {
            sessionController.getAllManagedTorrent().forEach {
                val handle: TorrentHandle? = sessionController.findHandle(it)
                // TODO: if handle is not valid we have to do remove it from session if necessary
                // TODO: and remove it from managed torrents also need to report to UI they decide to add it if they want
                if (handle != null) {
                    if (handle.isValid)
                        handleTorrentProgress(handle)
                    else {
                        logTorrent("found handler is not valid!", it)
                    }
                } else {
                    logTorrent("couldn't find handler!", it)
                }
            }
        }
    }

    override suspend fun getTorrentOverview(infoHash: String): TorrentOverview? {
        sessionController.findHandle(infoHash)?.let { handle ->
            if (handle.isValid) {
                val state = handle.status()
                val info = handle.torrentFile()
                return TorrentOverview(
                    handle.name(), infoHash, info?.totalSize() ?: 0,
                    info?.numPieces() ?: 0, info?.pieceLength() ?: 0,
                    state.progress(), TorrentUserState.ACTIVE, info?.creator() ?: "",
                    info?.comment() ?: "", (info?.creationDate() ?: 0) * 1000,
                    info?.isPrivate ?: false, state.lastSeenComplete()
                )
            }
        }
        readTorrentFileFromCache(infoHash)?.let {
            val info = TorrentInfo(it)
            return TorrentOverview(
                info.name(), infoHash, info.totalSize(), info.numPieces(),
                info.pieceLength(), 0f, TorrentUserState.PAUSED, info.creator(),
                info.comment(), info.creationDate() * 1000, info.isPrivate, 0
            )
        }

        return null
    }


    override suspend fun addTorrent(identifier: TorrentIdentifier) {
        var torrentInfo: TorrentInfo? = null
        readTorrentFileFromCache(identifier.infoHash)?.let {
            torrentInfo = getTorrentInfo(it)
        }
        sessionController.addTorrent(identifier.infoHash, identifier.magnet, torrentInfo)
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

            while (sessionController.stats().dhtNodes() < 10 && times < 100) {
                delay(100)
                times += 1
            }
            val bytes: ByteArray? = sessionController.fetchMagnet(magnet, 30)
            return@withContext getTorrentModel(bytes)
        }

    private fun getTorrentInfo(data: ByteArray): TorrentInfo {
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

    private fun saveTorrentFileToCache(infoHash: String, data: ByteArray) {
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
        return sessionController.removeTorrent(infoHash)
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