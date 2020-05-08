package app.shynline.torient.torrent.torrent

import app.shynline.torient.common.observable.BaseObservable
import app.shynline.torient.torrent.events.*
import app.shynline.torient.torrent.states.ManageState
import app.shynline.torient.torrent.states.TorrentDownloadingState
import com.frostwire.jlibtorrent.AlertListener
import com.frostwire.jlibtorrent.Sha1Hash
import com.frostwire.jlibtorrent.TorrentHandle
import com.frostwire.jlibtorrent.TorrentStatus
import com.frostwire.jlibtorrent.alerts.*
import kotlinx.coroutines.*


abstract class BaseTorrent : BaseObservable<Torrent.Listener>(), Torrent, AlertListener {
    protected val managedTorrents: MutableMap<String, ManageState> = hashMapOf()

    protected lateinit var torrentScope: CoroutineScope
    protected open fun start() {
        torrentScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    protected open fun stop() {
        torrentScope.cancel()
    }

    protected abstract fun findHandle(sha1: Sha1Hash): TorrentHandle?

    protected fun handleTorrentProgress(_handle: TorrentHandle) {
        val handle = findHandle(_handle.infoHash()) ?: return
        val statue = handle.status()
        val event: TorrentProgressEvent? = when (statue.state()) {
            TorrentStatus.State.CHECKING_FILES -> TorrentProgressEvent(
                handle.infoHash().toHex(),
                TorrentDownloadingState.CHECKING_FILES,
                progress = statue.progress()
            )
            TorrentStatus.State.DOWNLOADING_METADATA -> TorrentProgressEvent(
                handle.infoHash().toHex(),
                TorrentDownloadingState.DOWNLOADING_METADATA,
                progress = statue.progress()
            )
            TorrentStatus.State.DOWNLOADING -> TorrentProgressEvent(
                handle.infoHash().toHex(),
                TorrentDownloadingState.DOWNLOADING,
                progress = statue.progress(),
                downloadRate = statue.downloadRate(),
                uploadRate = statue.uploadRate(),
                maxPeers = statue.listPeers(),
                connectedPeers = statue.numPeers()
            )
            TorrentStatus.State.FINISHED -> TorrentProgressEvent(
                handle.infoHash().toHex(),
                TorrentDownloadingState.FINISHED
            )
            TorrentStatus.State.SEEDING -> TorrentProgressEvent(
                handle.infoHash().toHex(),
                TorrentDownloadingState.SEEDING,
                progress = statue.progress(),
                downloadRate = statue.downloadRate(),
                uploadRate = statue.uploadRate(),
                maxPeers = statue.listPeers(),
                connectedPeers = statue.numPeers()
            )
            TorrentStatus.State.ALLOCATING -> TorrentProgressEvent(
                handle.infoHash().toHex(),
                TorrentDownloadingState.ALLOCATING,
                progress = statue.progress()
            )
            TorrentStatus.State.CHECKING_RESUME_DATA -> TorrentProgressEvent(
                handle.infoHash().toHex(),
                TorrentDownloadingState.CHECKING_RESUME_DATA,
                progress = statue.progress()
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
                getListeners().forEach { listener ->
                    listener.onStatReceived(event)
                }
            }
        }
    }

    override suspend fun getManagedTorrentState(infoHash: String): ManageState? {
        return managedTorrents[infoHash]
    }

    override suspend fun getAllManagedTorrentStats(): Map<String, ManageState> {
        return managedTorrents.toMap()
    }


    private fun onAlertListenSucceeded(listenSucceededAlert: ListenSucceededAlert) {
        // TODO
    }

    private fun onAlertAddTorrentAlert(addTorrentAlert: AddTorrentAlert) {
        val handle = findHandle(addTorrentAlert.handle().infoHash()) ?: return

        val infoHash = addTorrentAlert.params().infoHash().toHex()
        managedTorrents[infoHash] = ManageState.ADDED
        val succeed = !addTorrentAlert.error().isError
        if (succeed) {
            handle.resume()
        }
        getListeners().forEach {
            it.onStatReceived(
                AddTorrentEvent(
                    infoHash,
                    succeed
                )
            )
        }
    }

    private fun onAlertSaveResumeData(saveResumeDataAlert: SaveResumeDataAlert) {
        // TODO
    }

    private fun onAlertBlockFinished(blockFinishedAlert: BlockFinishedAlert) {
//        handleTorrentProgress(blockFinishedAlert.handle())
        // TODO
    }

    private fun onAlertTorrentFinished(torrentFinishedAlert: TorrentFinishedAlert) {
        val handle = findHandle(torrentFinishedAlert.handle().infoHash()) ?: return
        val infoHash = handle.infoHash().toHex()
        managedTorrents[infoHash] = ManageState.FINISHED
        getListeners().forEach {
            it.onStatReceived(
                TorrentFinishedEvent(
                    infoHash
                )
            )
        }
    }

    private fun onAlertTorrentRemoved(torrentRemovedAlert: TorrentRemovedAlert) {
        val infoHash = torrentRemovedAlert.infoHash().toHex()
        managedTorrents.remove(infoHash)
        getListeners().forEach {
            it.onStatReceived(
                TorrentRemovedEvent(
                    infoHash
                )
            )
        }
    }

    private fun onAlertTorrentDeleted(torrentDeletedAlert: TorrentDeletedAlert) {
        // TODO
    }

//    private fun onAlertTorrentPaused(torrentPausedAlert: TorrentPausedAlert) {
////        val infoHash = torrentPausedAlert.handle().infoHash().toHex()
////        managedTorrents[infoHash] = ManageState.PAUSED
////        getListeners().forEach {
////            it.onStatReceived(
////                TorrentPausedEvent(
////                    infoHash
////                )
////            )
////        }
//    }

    private fun onAlertTorrentResumed(torrentResumedAlert: TorrentResumedAlert) {
        val infoHash = torrentResumedAlert.handle().infoHash().toHex()
        managedTorrents[infoHash] = ManageState.RESUMED
        getListeners().forEach {
            it.onStatReceived(
                TorrentResumedEvent(
                    infoHash
                )
            )
        }
    }

//    private fun onAlertTorrentChecked(torrentCheckedAlert: TorrentCheckedAlert) {
//        // TODO
//    }
//
//    private fun onAlertTorrentError(torrentErrorAlert: TorrentErrorAlert) {
//    }
//
//    private fun onAlertTorrentNeedCert(torrentNeedCertAlert: TorrentNeedCertAlert) {
//        // TODO
//    }
//
//    private fun onAlertIncomingConnection(incomingConnectionAlert: IncomingConnectionAlert) {
//        // TODO
//    }
//
//    private fun onAlertFastResumeRejected(fastResumeRejectedAlert: FastresumeRejectedAlert) {
//        // TODO
//    }
//
//    private fun onAlertMetaDataReceived(metadataReceivedAlert: MetadataReceivedAlert) {
//        // TODO
//    }
//
//    private fun onAlertMetaDataFailed(metadataFailedAlert: MetadataFailedAlert) {
//        // TODO
//    }
//
//    private fun onAlertFileCompleted(fileCompletedAlert: FileCompletedAlert) {
//        // TODO
//    }
//
//    private fun onAlertFileRenamed(fileRenamedAlert: FileRenamedAlert) {
//        // TODO
//    }
//
//    private fun onAlertFileRenameFailed(fileRenameFailedAlert: FileRenameFailedAlert) {
//        // TODO
//    }
//
//    private fun onAlertFileError(fileErrorAlert: FileErrorAlert) {
//        // TODO
//    }
//
//    private fun onAlertHashFailed(hashFailedAlert: HashFailedAlert) {
//        // TODO
//    }
//
//    private fun onAlertPortMap(portMapAlert: PortmapAlert) {
//        // TODO
//    }
//
//    private fun onAlertPortMapError(portMapErrorAlert: PortmapErrorAlert) {
//        // TODO
//    }
//
//    private fun onAlertPortMapLog(portMapLogAlert: PortmapLogAlert) {
//        // TODO
//    }
//
//    private fun onAlertAnnounce(trackerAnnounceAlert: TrackerAnnounceAlert) {
//        // TODO
//    }
//
//    private fun onAlertTrackerReply(trackerReplyAlert: TrackerReplyAlert) {
//        // TODO
//    }
//
//    private fun onAlertTrackerWarning(trackerWarningAlert: TrackerWarningAlert) {
//        // TODO
//    }
//
//    private fun onAlertTrackerError(trackerErrorAlert: TrackerErrorAlert) {
//        // TODO
//    }
//
//    private fun onAlertReadPiece(readPieceAlert: ReadPieceAlert) {
//        // TODO
//    }

//    private fun onAlertStateChanged(stateChangedAlert: StateChangedAlert) {
////        java.lang.IllegalArgumentException: No enum class com.frostwire.jlibtorrent.swig.torrent_status$state_t with value 1248/
////        Log.d(
////            "torientStateChanged",
////            "${stateChangedAlert.prevState.name} -> ${stateChangedAlert.state.name}"
////        )
//    }

//    private fun onAlertDhtReply(dhtReplyAlert: DhtReplyAlert) {
//        Log.d("fetchinmagnetdht","DHT reply")
//    }
//
//    private fun onAlertDhtBootstrap(dhtBootstrapAlert: DhtBootstrapAlert) {
//        Log.d("fetchinmagnetdht","DHT bootstrap")
//    }
//
//    private fun onAlertDhtGetPeers(dhtGetPeersAlert: DhtGetPeersAlert) {
//        Log.d("fetchinmagnetdht","DHT getPeers")
//    }
//
//    private fun onAlertExternalIp(externalIpAlert: ExternalIpAlert) {
//        // TODO
//    }
//
//    private fun onAlertStateUpdate(stateUpdateAlert: StateUpdateAlert) {
////        Log.d("torientStateUpdate","")
//    }
//
//    private fun onAlertSessionStats(sessionStatsAlert: SessionStatsAlert) {
//        // TODO
//    }
//
//    private fun onAlertScrapeReply(scrapeReplyAlert: ScrapeReplyAlert) {
//        // TODO
//    }
//
//    private fun onAlertScrapeFailed(scrapeFailedAlert: ScrapeFailedAlert) {
//        // TODO
//    }
//
//    private fun onAlertLsdPeer(lsdPeerAlert: LsdPeerAlert) {
//        // TODO
//    }
//
//    private fun onAlertPeerBlocked(peerBlockedAlert: PeerBlockedAlert) {
//        // TODO
//    }
//
//    private fun onAlertPerformance(performanceAlert: PerformanceAlert) {
//        // TODO
//    }
//
//    private fun onAlertPieceFinished(pieceFinishedAlert: PieceFinishedAlert) {
////        handleTorrentProgress(pieceFinishedAlert.handle())
//        // TODO
//    }
//
//    private fun onAlertSaveResumeDataFailed(saveResumeDataFailedAlert: SaveResumeDataFailedAlert) {
//        // TODO
//    }
//
//    private fun onAlertStorageMoved(storageMovedAlert: StorageMovedAlert) {
//        // TODO
//    }
//
//    private fun onAlertTorrentDeleteFailed(torrentDeleteFailedAlert: TorrentDeleteFailedAlert) {
//        // TODO
//    }
//
//    private fun onAlertUrlSeed(urlSeedAlert: UrlSeedAlert) {
//        // TODO
//    }
//
//    private fun onAlertInvalidRequest(invalidRequestAlert: InvalidRequestAlert) {
//        // TODO
//    }
//
//    private fun onAlertListenFailed(listenFailedAlert: ListenFailedAlert) {
//        // TODO
//    }
//
//    private fun onAlertPeerBan(peerBanAlert: PeerBanAlert) {
//        // TODO
//    }
//
//    private fun onAlertPeerConnect(peerConnectAlert: PeerConnectAlert) {
//        // TODO
//    }
//
//    private fun onAlertPeerDisconnected(peerDisconnectedAlert: PeerDisconnectedAlert) {
//        // TODO
//    }
//
//    private fun onAlertPeerError(peerErrorAlert: PeerErrorAlert) {
//        // TODO
//    }
//
//    private fun onAlertPeerSnubbed(peerSnubbedAlert: PeerSnubbedAlert) {
//        // TODO
//    }
//
//    private fun onAlertPeerUnSnubbed(peerUnSnubbedAlert: PeerUnsnubbedAlert) {
//        // TODO
//    }
//
//    private fun onAlertRequestDropped(requestDroppedAlert: RequestDroppedAlert) {
//        // TODO
//    }
//
//    private fun onAlertUdpError(udpErrorAlert: UdpErrorAlert) {
//        // TODO
//    }
//
//    private fun onAlertBlockDownloading(blockDownloadingAlert: BlockDownloadingAlert) {
//        // TODO
//    }
//
//    private fun onAlertBlockTimeout(blockTimeoutAlert: BlockTimeoutAlert) {
//        // TODO
//    }
//
//    private fun onAlertCacheFlushed(cacheFlushedAlert: CacheFlushedAlert) {
//        // TODO
//    }
//
//    private fun onAlertDhtAnnounce(dhtAnnounceAlert: DhtAnnounceAlert) {
//        Log.d("fetchinmagnetdht","DHT announce")
//    }
//
//    private fun onAlertStorageMovedFailed(storageMovedFailedAlert: StorageMovedFailedAlert) {
//        // TODO
//    }
//
//    private fun onAlertTrackerId(trackerIdAlert: TrackeridAlert) {
//        // TODO
//    }
//
//    private fun onAlertUnWantedBlock(unwantedBlockAlert: UnwantedBlockAlert) {
//        // TODO
//    }
//
//    private fun onAlertDhtError(dhtErrorAlert: DhtErrorAlert) {
//        Log.d("fetchinmagnetdht","DHT error ${dhtErrorAlert.error()?.message()}")
//    }
//
//    private fun onAlertDhtPut(dhtPutAlert: DhtPutAlert) {
//        Log.d("fetchinmagnetdht","DHT put")
//    }
//
//    private fun onAlertDhtMutableItem(dhtMutableItemAlert: DhtMutableItemAlert) {
//        Log.d("fetchinmagnetdht","DHT MutableItem")
//    }
//
//    private fun onAlertDhtImmutableItem(dhtImmutableItemAlert: DhtImmutableItemAlert) {
//        Log.d("fetchinmagnetdht","DHT ImmutableItem")
//    }
//
//    private fun onAlertI2P(i2pAlert: I2pAlert) {
//        // TODO
//    }
//
//    private fun onAlertDhtOutgoingGetPeers(dhtOutgoingGetPeersAlert: DhtOutgoingGetPeersAlert) {
//        // TODO
//    }
//
//    private fun onAlertLog(logAlert: LogAlert) {
////        Log.d("torientLibLog", "log: ${logAlert.logMessage()}")
//    }
//
//    private fun onAlertTorrentLog(torrentLogAlert: TorrentLogAlert) {
//        // TODO
//    }
//
//    private fun onAlertPeerLog(peerLogAlert: PeerLogAlert) {
//        // TODO
//    }
//
//    private fun onAlertLsdError(lsdErrorAlert: LsdErrorAlert) {
//        // TODO
//    }
//
//    private fun onAlertDhtStats(dhtStatsAlert: DhtStatsAlert) {
//        // TODO
//    }
//
//    private fun onAlertInComingRequest(incomingRequestAlert: IncomingRequestAlert) {
//        // TODO
//    }
//
//    private fun onAlertDhtLog(dhtLogAlert: DhtLogAlert) {
//        Log.d("flogetchinmagnetdht","DHT log ${dhtLogAlert.logMessage()}")
//    }
//
//    private fun onAlertDhtPkt(dhtPktAlert: DhtPktAlert) {
////        Log.d("torientDhtLog", dhtPktAlert.message())
//    }
//
//    private fun onAlertDhtGetPeersReply(dhtGetPeersReplyAlert: DhtGetPeersReplyAlert) {
//        // TODO
//    }
//
//    private fun onAlertDhtDirectResponse(dhtDirectResponseAlert: DhtDirectResponseAlert) {
//        // TODO
//    }
//
//    private fun onAlertPickedLog(pickerLogAlert: PickerLogAlert) {
//        // TODO
//    }
//
//    private fun onAlertSessionError(sessionErrorAlert: SessionErrorAlert) {
//    }
//
//    private fun onAlertDhtLiveNodes(dhtLiveNodesAlert: DhtLiveNodesAlert) {
//        dhtNodes = dhtLiveNodesAlert.numNodes()
//    }
//
//    private fun onAlertSessionStatsHeader(sessionStatsHeaderAlert: SessionStatsHeaderAlert) {
//        // TODO
//    }
//
//    private fun onAlertDhtSampleInfoHashes(dhtSampleInfoHashesAlert: DhtSampleInfohashesAlert) {
//        // TODO
//    }
//
//    private fun onAlertBlockUploaded(blockUploadedAlert: BlockUploadedAlert) {
//        // TODO
//    }

    /**
     * alert is sent by libTorrent when an event happens
     *
     * @param p0
     */
    override fun alert(p0: Alert<*>?) {
//        Log.d("aler5tismyawesome4", "alert type: ${p0?.type()?.name}")
        // Bad practice right there
        GlobalScope.launch(Dispatchers.IO) {

            when (p0?.type()) {
                AlertType.ADD_TORRENT -> {
                    onAlertAddTorrentAlert(p0 as AddTorrentAlert)
                }
                AlertType.BLOCK_FINISHED -> {
                    onAlertBlockFinished(p0 as BlockFinishedAlert)
                }
                AlertType.TORRENT_FINISHED -> {
                    onAlertTorrentFinished(p0 as TorrentFinishedAlert)
                }
                AlertType.SAVE_RESUME_DATA -> {
                    onAlertSaveResumeData(p0 as SaveResumeDataAlert)
                }
//                AlertType.STATS -> {
//                    onAlertStats(p0 as StatsAlert)
//                }
                AlertType.LISTEN_SUCCEEDED -> {
                    onAlertListenSucceeded(p0 as ListenSucceededAlert)
                }
                AlertType.TORRENT_REMOVED -> {
                    onAlertTorrentRemoved(p0 as TorrentRemovedAlert)
                }
                AlertType.TORRENT_DELETED -> {
                    onAlertTorrentDeleted(p0 as TorrentDeletedAlert)
                }
//                AlertType.TORRENT_PAUSED -> {
//                    onAlertTorrentPaused(p0 as TorrentPausedAlert)
//                }
                AlertType.TORRENT_RESUMED -> {
                    onAlertTorrentResumed(p0 as TorrentResumedAlert)
                }
//                AlertType.TORRENT_CHECKED -> {
//                    onAlertTorrentChecked(p0 as TorrentCheckedAlert)
//                }
//                AlertType.TORRENT_ERROR -> {
//                    onAlertTorrentError(p0 as TorrentErrorAlert)
//                }
//                AlertType.TORRENT_NEED_CERT -> {
//                    onAlertTorrentNeedCert(p0 as TorrentNeedCertAlert)
//                }
//                AlertType.INCOMING_CONNECTION -> {
//                    onAlertIncomingConnection(p0 as IncomingConnectionAlert)
//                }
//                AlertType.FASTRESUME_REJECTED -> {
//                    onAlertFastResumeRejected(p0 as FastresumeRejectedAlert)
//                }
//                AlertType.METADATA_RECEIVED -> {
//                    onAlertMetaDataReceived(p0 as MetadataReceivedAlert)
//                }
//                AlertType.METADATA_FAILED -> {
//                    onAlertMetaDataFailed(p0 as MetadataFailedAlert)
//                }
//                AlertType.FILE_COMPLETED -> {
//                    onAlertFileCompleted(p0 as FileCompletedAlert)
//                }
//                AlertType.FILE_RENAMED -> {
//                    onAlertFileRenamed(p0 as FileRenamedAlert)
//                }
//                AlertType.FILE_RENAME_FAILED -> {
//                    onAlertFileRenameFailed(p0 as FileRenameFailedAlert)
//                }
//                AlertType.FILE_ERROR -> {
//                    onAlertFileError(p0 as FileErrorAlert)
//                }
//                AlertType.HASH_FAILED -> {
//                    onAlertHashFailed(p0 as HashFailedAlert)
//                }
//                AlertType.PORTMAP -> {
//                    onAlertPortMap(p0 as PortmapAlert)
//                }
//                AlertType.PORTMAP_ERROR -> {
//                    onAlertPortMapError(p0 as PortmapErrorAlert)
//                }
//                AlertType.PORTMAP_LOG -> {
//                    onAlertPortMapLog(p0 as PortmapLogAlert)
//                }
//                AlertType.TRACKER_ANNOUNCE -> {
//                    onAlertAnnounce(p0 as TrackerAnnounceAlert)
//                }
//                AlertType.TRACKER_REPLY -> {
//                    onAlertTrackerReply(p0 as TrackerReplyAlert)
//                }
//                AlertType.TRACKER_WARNING -> {
//                    onAlertTrackerWarning(p0 as TrackerWarningAlert)
//                }
//                AlertType.TRACKER_ERROR -> {
//                    onAlertTrackerError(p0 as TrackerErrorAlert)
//                }
//                AlertType.READ_PIECE -> {
//                    onAlertReadPiece(p0 as ReadPieceAlert)
//                }
//                AlertType.STATE_CHANGED -> {
//                    onAlertStateChanged(p0 as StateChangedAlert)
//                }
//                AlertType.DHT_REPLY -> {
//                    onAlertDhtReply(p0 as DhtReplyAlert)
//                }
//                AlertType.DHT_BOOTSTRAP -> {
//                    onAlertDhtBootstrap(p0 as DhtBootstrapAlert)
//                }
//                AlertType.DHT_GET_PEERS -> {
//                    onAlertDhtGetPeers(p0 as DhtGetPeersAlert)
//                }
//                AlertType.EXTERNAL_IP -> {
//                    onAlertExternalIp(p0 as ExternalIpAlert)
//                }
//                AlertType.STATE_UPDATE -> {
//                    onAlertStateUpdate(p0 as StateUpdateAlert)
//                }
//                AlertType.SESSION_STATS -> {
//                    onAlertSessionStats(p0 as SessionStatsAlert)
//                }
//                AlertType.SCRAPE_REPLY -> {
//                    onAlertScrapeReply(p0 as ScrapeReplyAlert)
//                }
//                AlertType.SCRAPE_FAILED -> {
//                    onAlertScrapeFailed(p0 as ScrapeFailedAlert)
//                }
//                AlertType.LSD_PEER -> {
//                    onAlertLsdPeer(p0 as LsdPeerAlert)
//                }
//                AlertType.PEER_BLOCKED -> {
//                    onAlertPeerBlocked(p0 as PeerBlockedAlert)
//                }
//                AlertType.PERFORMANCE -> {
//                    onAlertPerformance(p0 as PerformanceAlert)
//                }
//                AlertType.PIECE_FINISHED -> {
//                    onAlertPieceFinished(p0 as PieceFinishedAlert)
//                }
//                AlertType.SAVE_RESUME_DATA_FAILED -> {
//                    onAlertSaveResumeDataFailed(p0 as SaveResumeDataFailedAlert)
//                }
//                AlertType.STORAGE_MOVED -> {
//                    onAlertStorageMoved(p0 as StorageMovedAlert)
//                }
//                AlertType.TORRENT_DELETE_FAILED -> {
//                    onAlertTorrentDeleteFailed(p0 as TorrentDeleteFailedAlert)
//                }
//                AlertType.URL_SEED -> {
//                    onAlertUrlSeed(p0 as UrlSeedAlert)
//                }
//                AlertType.INVALID_REQUEST -> {
//                    onAlertInvalidRequest(p0 as InvalidRequestAlert)
//                }
//                AlertType.LISTEN_FAILED -> {
//                    onAlertListenFailed(p0 as ListenFailedAlert)
//                }
//                AlertType.PEER_BAN -> {
//                    onAlertPeerBan(p0 as PeerBanAlert)
//                }
//                AlertType.PEER_CONNECT -> {
//                    onAlertPeerConnect(p0 as PeerConnectAlert)
//                }
//                AlertType.PEER_DISCONNECTED -> {
//                    onAlertPeerDisconnected(p0 as PeerDisconnectedAlert)
//                }
//                AlertType.PEER_ERROR -> {
//                    onAlertPeerError(p0 as PeerErrorAlert)
//                }
//                AlertType.PEER_SNUBBED -> {
//                    onAlertPeerSnubbed(p0 as PeerSnubbedAlert)
//                }
//                AlertType.PEER_UNSNUBBED -> {
//                    onAlertPeerUnSnubbed(p0 as PeerUnsnubbedAlert)
//                }
//                AlertType.REQUEST_DROPPED -> {
//                    onAlertRequestDropped(p0 as RequestDroppedAlert)
//                }
//                AlertType.UDP_ERROR -> {
//                    onAlertUdpError(p0 as UdpErrorAlert)
//                }
//                AlertType.BLOCK_DOWNLOADING -> {
//                    onAlertBlockDownloading(p0 as BlockDownloadingAlert)
//                }
//                AlertType.BLOCK_TIMEOUT -> {
//                    onAlertBlockTimeout(p0 as BlockTimeoutAlert)
//                }
//                AlertType.CACHE_FLUSHED -> {
//                    onAlertCacheFlushed(p0 as CacheFlushedAlert)
//                }
//                AlertType.DHT_ANNOUNCE -> {
//                    onAlertDhtAnnounce(p0 as DhtAnnounceAlert)
//                }
//                AlertType.STORAGE_MOVED_FAILED -> {
//                    onAlertStorageMovedFailed(p0 as StorageMovedFailedAlert)
//                }
//                AlertType.TRACKERID -> {
//                    onAlertTrackerId(p0 as TrackeridAlert)
//                }
//                AlertType.UNWANTED_BLOCK -> {
//                    onAlertUnWantedBlock(p0 as UnwantedBlockAlert)
//                }
//                AlertType.DHT_ERROR -> {
//                    onAlertDhtError(p0 as DhtErrorAlert)
//                }
//                AlertType.DHT_PUT -> {
//                    onAlertDhtPut(p0 as DhtPutAlert)
//                }
//                AlertType.DHT_MUTABLE_ITEM -> {
//                    onAlertDhtMutableItem(p0 as DhtMutableItemAlert)
//                }
//                AlertType.DHT_IMMUTABLE_ITEM -> {
//                    onAlertDhtImmutableItem(p0 as DhtImmutableItemAlert)
//                }
//                AlertType.I2P -> {
//                    onAlertI2P(p0 as I2pAlert)
//                }
//                AlertType.DHT_OUTGOING_GET_PEERS -> {
//                    onAlertDhtOutgoingGetPeers(p0 as DhtOutgoingGetPeersAlert)
//                }
//                AlertType.LOG -> {
//                    onAlertLog(p0 as LogAlert)
//                }
//                AlertType.TORRENT_LOG -> {
//                    onAlertTorrentLog(p0 as TorrentLogAlert)
//                }
//                AlertType.PEER_LOG -> {
//                    onAlertPeerLog(p0 as PeerLogAlert)
//                }
//                AlertType.LSD_ERROR -> {
//                    onAlertLsdError(p0 as LsdErrorAlert)
//                }
//                AlertType.DHT_STATS -> {
//                    onAlertDhtStats(p0 as DhtStatsAlert)
//                }
//                AlertType.INCOMING_REQUEST -> {
//                    onAlertInComingRequest(p0 as IncomingRequestAlert)
//                }
//                AlertType.DHT_LOG -> {
//                    onAlertDhtLog(p0 as DhtLogAlert)
//                }
//                AlertType.DHT_PKT -> {
//                    onAlertDhtPkt(p0 as DhtPktAlert)
//                }
//                AlertType.DHT_GET_PEERS_REPLY -> {
//                    onAlertDhtGetPeersReply(p0 as DhtGetPeersReplyAlert)
//                }
//                AlertType.DHT_DIRECT_RESPONSE -> {
//                    onAlertDhtDirectResponse(p0 as DhtDirectResponseAlert)
//                }
//                AlertType.PICKER_LOG -> {
//                    onAlertPickedLog(p0 as PickerLogAlert)
//                }
//                AlertType.SESSION_ERROR -> {
//                    onAlertSessionError(p0 as SessionErrorAlert)
//                }
//                AlertType.DHT_LIVE_NODES -> {
//                    onAlertDhtLiveNodes(p0 as DhtLiveNodesAlert)
//                }
//                AlertType.SESSION_STATS_HEADER -> {
//                    onAlertSessionStatsHeader(p0 as SessionStatsHeaderAlert)
//                }
//                AlertType.DHT_SAMPLE_INFOHASHES -> {
//                    onAlertDhtSampleInfoHashes(p0 as DhtSampleInfohashesAlert)
//                }
//                AlertType.BLOCK_UPLOADED -> {
//                    onAlertBlockUploaded(p0 as BlockUploadedAlert)
//                }
                AlertType.ALERTS_DROPPED -> {
                }
                AlertType.UNKNOWN -> {
                }
                null -> {
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
        return null
    }


}