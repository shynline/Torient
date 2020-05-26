package app.shynline.torient.torrent.torrent

import app.shynline.torient.common.observable.BaseObservable
import app.shynline.torient.database.datasource.torrent.InternalTorrentDataSource
import app.shynline.torient.database.datasource.torrentfilepriority.TorrentFilePriorityDataSource
import app.shynline.torient.model.TorrentFilePriority
import app.shynline.torient.model.TorrentModel
import app.shynline.torient.torrent.events.TorrentMetaDataEvent
import app.shynline.torient.torrent.events.TorrentProgressEvent
import app.shynline.torient.torrent.states.TorrentDownloadingState
import com.frostwire.jlibtorrent.*
import com.frostwire.jlibtorrent.alerts.AddTorrentAlert
import com.frostwire.jlibtorrent.alerts.Alert
import com.frostwire.jlibtorrent.alerts.AlertType
import com.frostwire.jlibtorrent.alerts.MetadataReceivedAlert
import kotlinx.coroutines.*


abstract class BaseTorrent(
    protected open val internalTorrentDataSource: InternalTorrentDataSource,
    protected open val torrentFilePriorityDataSource: TorrentFilePriorityDataSource
) : BaseObservable<Torrent.Listener>(), Torrent, AlertListener {
    protected val managedTorrents: MutableMap<String, TorrentStatus.State?> = hashMapOf()

    protected lateinit var torrentScope: CoroutineScope
    protected open fun start() {
        torrentScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    protected open fun stop() {
        torrentScope.cancel()
    }

    protected abstract fun applyPreference(infoHash: String?)

    protected abstract fun findHandle(sha1: Sha1Hash): TorrentHandle?

    protected abstract fun saveTorrentFileToCache(infoHash: String, data: ByteArray)

    protected suspend fun handleTorrentProgress(handle: TorrentHandle) {
        val status = handle.status()
        val infoHash = handle.infoHash().toHex()
        val event: TorrentProgressEvent? = when (status.state()) {
            TorrentStatus.State.CHECKING_FILES -> TorrentProgressEvent(
                handle.infoHash().toHex(),
                TorrentDownloadingState.CHECKING_FILES,
                progress = status.progress()
            )
            TorrentStatus.State.DOWNLOADING_METADATA -> TorrentProgressEvent(
                handle.infoHash().toHex(),
                TorrentDownloadingState.DOWNLOADING_METADATA
            )
            TorrentStatus.State.DOWNLOADING -> {
                // If the last state was Downloading meta data
                // It means we have the meta data now
                // and we have to show it to user
                checkIfMetaDataDownloaded(infoHash, handle)
                val tpe = TorrentProgressEvent(
                    infoHash,
                    TorrentDownloadingState.DOWNLOADING,
                    progress = status.progress(),
                    downloadRate = status.downloadPayloadRate(),
                    uploadRate = status.uploadPayloadRate(),
                    maxPeers = status.listPeers(),
                    connectedPeers = status.numPeers()
                )
                internalTorrentDataSource.setTorrentProgress(
                    infoHash,
                    tpe.progress,
                    lastSeenComplete = status.lastSeenComplete(),
                    fileProgress = handle.fileProgress()
                )
                tpe
            }
            TorrentStatus.State.FINISHED -> {
                // This is a rare case when the file is so small
                // Also it happens when whole downloading process being done
                // in background process
                checkIfMetaDataDownloaded(infoHash, handle)
                val tpe = TorrentProgressEvent(
                    handle.infoHash().toHex(),
                    TorrentDownloadingState.FINISHED
                )
                internalTorrentDataSource.setTorrentFinished(
                    infoHash, true,
                    fileProgress = handle.fileProgress()
                )
                tpe
            }
            TorrentStatus.State.SEEDING -> {
                // This is a rare case when the file is so small
                // Also it happens when whole downloading process being done
                // in background process
                checkIfMetaDataDownloaded(infoHash, handle)
                internalTorrentDataSource.setTorrentFinished(
                    infoHash,
                    true,
                    fileProgress = handle.fileProgress()
                )
                TorrentProgressEvent(
                    handle.infoHash().toHex(),
                    TorrentDownloadingState.SEEDING,
                    downloadRate = status.downloadRate(),
                    uploadRate = status.uploadRate(),
                    maxPeers = status.listPeers(),
                    connectedPeers = status.numPeers()
                )
            }
            TorrentStatus.State.ALLOCATING -> TorrentProgressEvent(
                handle.infoHash().toHex(),
                TorrentDownloadingState.ALLOCATING
            )
            TorrentStatus.State.CHECKING_RESUME_DATA -> TorrentProgressEvent(
                handle.infoHash().toHex(),
                TorrentDownloadingState.CHECKING_RESUME_DATA
            )
            TorrentStatus.State.UNKNOWN -> TorrentProgressEvent(
                handle.infoHash().toHex(),
                TorrentDownloadingState.UNKNOWN
            )
            null -> {
                null
            }
        }
        event?.let { ev ->
            if (ev.state != TorrentDownloadingState.UNKNOWN) {
                managedTorrents[infoHash] = status.state()
                getListeners().forEach { listener ->
                    listener.onStatReceived(event)
                }
            }
        }
    }

    private fun checkIfMetaDataDownloaded(infoHash: String, handle: TorrentHandle) {
        if (managedTorrents[infoHash] == TorrentStatus.State.DOWNLOADING_METADATA) {
            val metaDataEvent = TorrentMetaDataEvent(
                infoHash,
                TorrentModel.from(handle.torrentFile())
            )
            // Cache it in torrent storage
            saveTorrentFileToCache(infoHash, handle.torrentFile().bencode())
            getListeners().forEach {
                it.onStatReceived(metaDataEvent)
            }
        }
    }

    override suspend fun getAllManagedTorrents(): List<String> {
        return managedTorrents.keys.toList()
    }


    private fun onAlertAddTorrentAlert(addTorrentAlert: AddTorrentAlert) {
        val infoHash = addTorrentAlert.handle().infoHash()
        val handle = findHandle(infoHash) ?: return
        if (!addTorrentAlert.error().isError) {
            handle.resume()
            applyPreference(infoHash.toHex())
            GlobalScope.launch {
                val p = torrentFilePriorityDataSource.getPriority(infoHash.toHex())
                if (handle.torrentFile()?.isValid == true) { // Torrent meta data is present
                    if (p.filePriority == null) {
                        // Generate default priorities
                        p.filePriority = MutableList(
                            handle.torrentFile().numFiles()
                        ) { TorrentFilePriority.default() }
                        // Update database with generated priorities
                        torrentFilePriorityDataSource.setPriority(p)
                    }
                    setFilesPriority(infoHash.toHex(), p.filePriority!!)
                }
            }
        }
    }

    private fun onAlertMetaDataReceived(metadataReceivedAlert: MetadataReceivedAlert) {
        val torrentInfo: TorrentInfo? = TorrentInfo(metadataReceivedAlert.torrentData())
        torrentInfo?.let {
            val handle = findHandle(it.infoHash()) ?: return
            GlobalScope.launch {
                val p = torrentFilePriorityDataSource.getPriority(it.infoHash().toHex())
                if (handle.torrentFile().isValid) { // Torrent meta data is present
                    if (p.filePriority == null) {
                        // Generate default priorities
                        p.filePriority = MutableList(
                            handle.torrentFile().numFiles()
                        ) { TorrentFilePriority.default() }
                        // Update database with generated priorities
                        torrentFilePriorityDataSource.setPriority(p)
                    }
                    setFilesPriority(it.infoHash().toHex(), p.filePriority!!)
                }
            }
        }
    }

    /**
     * alert is sent by libTorrent when an event happens
     *
     * @param p0
     */
    override fun alert(p0: Alert<*>?) {
        // Bad practice right there
        GlobalScope.launch(Dispatchers.IO) {

            when (p0?.type()) {
                AlertType.ADD_TORRENT -> {
                    onAlertAddTorrentAlert(p0 as AddTorrentAlert)
                }
                AlertType.METADATA_RECEIVED -> {
                    onAlertMetaDataReceived(p0 as MetadataReceivedAlert)
                }
            }
        }
    }

    /**
     * filter the events which a libTorrent have to send
     * return null to not filter anything
     *
     * @return
     */
    override fun types(): IntArray? {
        return intArrayOf(AlertType.ADD_TORRENT.swig(), AlertType.METADATA_RECEIVED.swig())
    }


}