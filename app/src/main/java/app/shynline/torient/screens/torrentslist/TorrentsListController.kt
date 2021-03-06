package app.shynline.torient.screens.torrentslist

import app.shynline.torient.domain.database.common.states.TorrentUserState
import app.shynline.torient.domain.database.datasource.torrent.TorrentDataSource
import app.shynline.torient.domain.database.datasource.torrentfilepriority.TorrentFilePriorityDataSource
import app.shynline.torient.domain.mediator.SubscriptionMediator
import app.shynline.torient.domain.mediator.TorrentMediator
import app.shynline.torient.domain.mediator.usecases.CalculateTorrentModelFilesProgressUseCase
import app.shynline.torient.domain.models.TorrentModel
import app.shynline.torient.domain.torrentmanager.common.events.TorrentEvent
import app.shynline.torient.domain.torrentmanager.common.events.TorrentMetaDataEvent
import app.shynline.torient.domain.torrentmanager.common.events.TorrentProgressEvent
import app.shynline.torient.domain.torrentmanager.common.states.TorrentDownloadingState
import app.shynline.torient.screens.common.BaseController
import app.shynline.torient.screens.common.navigationhelper.PageNavigationHelper
import app.shynline.torient.screens.common.requesthelper.FragmentRequestHelper
import app.shynline.torient.screens.common.requesthelper.REQUEST_ID_OPEN_TORRENT_FILE
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class TorrentsListController(
    coroutineDispatcher: CoroutineDispatcher,
    private val subscriptionMediator: SubscriptionMediator,
    private val torrentDataSource: TorrentDataSource,
    private val torrentMediator: TorrentMediator,
    private val torrentFilePriorityDataSource: TorrentFilePriorityDataSource,
    private val calculateTorrentModelFilesProgressUseCase: CalculateTorrentModelFilesProgressUseCase
) : BaseController(coroutineDispatcher), TorrentListViewMvc.Listener,
    SubscriptionMediator.Listener {

    private var viewMvc: TorrentListViewMvc? = null
    private var fragmentRequestHelper: FragmentRequestHelper? = null
    private var pageNavigationHelper: PageNavigationHelper? = null
    private val managedTorrents: MutableMap<String, TorrentModel> = hashMapOf()

    fun bind(
        viewMvc: TorrentListViewMvc, fragmentRequestHelper: FragmentRequestHelper,
        pageNavigationHelper: PageNavigationHelper
    ) {
        this.viewMvc = viewMvc
        this.fragmentRequestHelper = fragmentRequestHelper
        this.pageNavigationHelper = pageNavigationHelper
    }

    override fun cleanUp() {
        viewMvc = null
        fragmentRequestHelper = null
        pageNavigationHelper = null
    }

    override fun onStatReceived(torrentEvent: TorrentEvent) {
        controllerScope.launch {
            when (torrentEvent) {
                is TorrentProgressEvent -> {
                    managedTorrents[torrentEvent.infoHash]?.let { torrent ->
                        when (torrentEvent.state) {
                            TorrentDownloadingState.UNKNOWN -> {
                            }
                            TorrentDownloadingState.ALLOCATING -> {
                                torrent.downloadingState = torrentEvent.state
                                viewMvc?.notifyItemUpdate(torrent.infoHash)
                            }
                            TorrentDownloadingState.CHECKING_FILES -> {
                                torrent.downloadingState = torrentEvent.state
                                torrent.progress = torrentEvent.progress
                                viewMvc?.notifyItemUpdate(torrent.infoHash)
                            }
                            TorrentDownloadingState.CHECKING_RESUME_DATA -> {
                                torrent.downloadingState = torrentEvent.state
                                viewMvc?.notifyItemUpdate(torrent.infoHash)
                            }
                            TorrentDownloadingState.DOWNLOADING -> {
                                torrent.downloadingState = torrentEvent.state
                                torrent.downloadRate = torrentEvent.downloadRate
                                torrent.uploadRate = torrentEvent.uploadRate
                                torrent.progress = torrentEvent.progress
                                torrent.connectedPeers = torrentEvent.connectedPeers
                                torrent.maxPeers = torrentEvent.maxPeers
                                calculateTorrentModelFilesProgressUseCase(
                                    CalculateTorrentModelFilesProgressUseCase.In(
                                        torrent, torrentEvent.fileProgress
                                    )
                                )
                                viewMvc?.notifyItemUpdate(torrent.infoHash)
                            }
                            TorrentDownloadingState.DOWNLOADING_METADATA -> {
                                torrent.downloadingState = torrentEvent.state
                                viewMvc?.notifyItemUpdate(torrent.infoHash)
                            }
                            // It's less likely we catch this stat if torrent is going to seed
                            TorrentDownloadingState.FINISHED -> {
                                torrent.downloadingState = torrentEvent.state
                                torrent.finished = true
                                viewMvc?.notifyItemUpdate(torrent.infoHash)
                            }
                            TorrentDownloadingState.SEEDING -> {
                                torrent.finished = true
                                torrent.downloadingState = torrentEvent.state
                                torrent.downloadRate = torrentEvent.downloadRate
                                torrent.uploadRate = torrentEvent.uploadRate
                                torrent.connectedPeers = torrentEvent.connectedPeers
                                torrent.maxPeers = torrentEvent.maxPeers
                                viewMvc?.notifyItemUpdate(torrent.infoHash)
                            }
                        }
                    }
                }
                is TorrentMetaDataEvent -> {
                    val schema = torrentDataSource.getTorrent(torrentEvent.infoHash)!!
                    managedTorrents[torrentEvent.infoHash]?.let {
                        it.name = torrentEvent.torrentModel.name
                        it.totalSize = torrentEvent.torrentModel.totalSize
                        it.author = torrentEvent.torrentModel.author
                        it.comment = torrentEvent.torrentModel.comment
                        it.hexHash = torrentEvent.torrentModel.hexHash
                        it.torrentFile = torrentEvent.torrentModel.torrentFile
                        it.filesSize = torrentEvent.torrentModel.filesSize
                        // File priority should not be null here
                        it.filePriority =
                            torrentFilePriorityDataSource.getPriority(it.infoHash).filePriority!!
                        calculateTorrentModelFilesProgressUseCase(
                            CalculateTorrentModelFilesProgressUseCase.In(
                                it, schema.fileProgress
                            )
                        )
                        viewMvc?.notifyItemUpdate(it.infoHash)
                    }
                }
            }
        }
    }

    override fun loadState(state: HashMap<String, Any>?) {
    }

    override fun saveState(): HashMap<String, Any>? {
        return null
    }

    private var queryingDataBaseJob: Job? = null

    override fun onStart() {
        viewMvc!!.registerListener(this)
        // Subscribe for previously being managed torrents
        subscriptionMediator.subscribe(this, managedTorrents.keys.toTypedArray())
        queryingDataBaseJob = queryDataBaseForChange()
    }

    override fun onStop() {
        viewMvc!!.unRegisterListener(this)
        subscriptionMediator.unsubscribe(this)
        queryingDataBaseJob?.cancel()
    }

    private fun queryDataBaseForChange() = controllerScope.launch {
        torrentDataSource.getTorrents().collect { torrentSchemas ->
            val torrentFilesProgress: MutableMap<String, List<Long>?> = mutableMapOf()
            // The Torrents which are being managed by the service so we shouldn't add them again
            val serviceManagedTorrents = torrentMediator.getAllManagedTorrents()
            // It's a flow so it will be called on each database transaction
            // Make a copy of our managed torrent to find out which torrents are not in database any more
            val removedTorrents = managedTorrents.keys.toMutableList()
            // Traversing through torrents in database
            torrentSchemas.forEach {
                torrentFilesProgress[it.infoHash] = it.fileProgress
                val torrentModel = torrentMediator.getTorrentModel(
                    infoHash = it.infoHash
                ) ?: TorrentModel(
                    infoHash = it.infoHash,
                    name = it.name,
                    magnet = it.magnet
                )
                torrentModel.apply {
                    userState = it.userState
                    finished = it.isFinished
                    progress = it.progress
                    dateAdded = it.dateAdded
                }
                // It's not managed by controller
                if (!managedTorrents.containsKey(it.infoHash)) {
                    // We add it to our managed list
                    managedTorrents[it.infoHash] = torrentModel

                    // If it's active torrent
                    if (it.userState == TorrentUserState.ACTIVE) {
                        // Subscribe to the service for this torrent
                        subscriptionMediator.addTorrent(
                            this@TorrentsListController,
                            it.infoHash
                        )
                        // Add it to torrent manager
                        torrentMediator.addTorrent(it.toIdentifier())
                    } else if (it.userState == TorrentUserState.PAUSED) {
                        // Safety remove and unsubscribe
                        torrentMediator.removeTorrent(it.infoHash)
                        subscriptionMediator.removeTorrent(
                            this@TorrentsListController,
                            it.infoHash
                        )
                    }
                } else {
                    // We have the torrent so we remove it from our removedTorrent
                    removedTorrents.remove(it.infoHash)
                    // Replace the model, it covers cases which metadata updated outside of
                    // this controller active state ( after onStop ) when it's waiting for metadata
                    managedTorrents[it.infoHash] = torrentModel

                    // Check if torrent is not managed by torrent manager
                    if (!serviceManagedTorrents.contains(it.infoHash)) {
                        // The torrent is not being manage by torrent manager
                        if (it.userState == TorrentUserState.ACTIVE) {
                            // Subscribe to the service for this torrent
                            subscriptionMediator.addTorrent(
                                this@TorrentsListController, it.infoHash
                            )
                            torrentMediator.addTorrent(it.toIdentifier())
                        }
                    } else {
                        // torrent manager is managing this torrent and torrent need to be stopped
                        if (it.userState == TorrentUserState.PAUSED) {
                            torrentMediator.removeTorrent(it.infoHash)
                            subscriptionMediator.removeTorrent(
                                this@TorrentsListController,
                                it.infoHash
                            )
                        }
                    }

                }
            }
            // If any torrent were removed in other screens it appear here
            // They might be already removed from torrent service in that screen
            // But we still check for making sure
            removedTorrents.forEach {
                managedTorrents.remove(it)
                if (serviceManagedTorrents.contains(it)) {
                    // The service is managing the torrent
                    // Request to remove it
                    torrentMediator.removeTorrent(it)
                }
            }
            // Managed torrent now is updated
            // For each managed torrent calculate progress
            managedTorrents.values.forEach { torrentModel ->
                torrentModel.filePriority =
                    torrentFilePriorityDataSource.getPriority(torrentModel.infoHash).filePriority
                calculateTorrentModelFilesProgressUseCase(
                    CalculateTorrentModelFilesProgressUseCase.In(
                        torrentModel, torrentFilesProgress[torrentModel.infoHash]
                    )
                )
            }
            viewMvc!!.showTorrents(managedTorrents.values.toList())
        }
    }


    override fun addTorrentMagnet() {
        pageNavigationHelper!!.showAddMagnetDialog()
    }


    override fun addTorrentFile() {
        fragmentRequestHelper!!.openTorrentFile(REQUEST_ID_OPEN_TORRENT_FILE)
    }

    override fun onRemoveTorrent(torrentModel: TorrentModel) {
        // Remove from UI
        viewMvc!!.removeTorrent(torrentModel.hexHash)
        // Remove controller cache
        managedTorrents.remove(torrentModel.infoHash)
        // Unsubscribe
        subscriptionMediator.removeTorrent(this, torrentModel.infoHash)
        controllerScope.launch {
            // Remove torrent if exists
            torrentMediator.removeTorrent(torrentModel.infoHash)
            // Remove files if exists
            torrentMediator.removeTorrentFiles(torrentModel.name)
            // Remove from database
            torrentDataSource.removeTorrent(torrentModel.infoHash)
            // Remove priorities
            torrentFilePriorityDataSource.removeTorrentFilePriority(torrentModel.infoHash)
        }
    }

    fun openTorrentFile(torrentData: ByteArray) = controllerScope.launch {
        val torrentDetail = torrentMediator.getTorrentModel(torrentFile = torrentData)
        if (torrentDetail != null) {
            pageNavigationHelper!!.showNewTorrentDialog(torrentDetail.infoHash)
        } else {
            // TODO notify user
        }

    }

    override fun onTorrentClicked(torrentModel: TorrentModel) {
        pageNavigationHelper!!.showTorrentOverView(torrentModel.infoHash, torrentModel.name)
    }

    override fun onCopyMagnetRequested(torrentModel: TorrentModel) {
        fragmentRequestHelper!!.copyMagnetToClipBoard(torrentModel.name, torrentModel.magnet)
    }

    override fun onSaveToDownloadRequested(torrentModel: TorrentModel) {
        fragmentRequestHelper!!.saveToDownload(torrentModel.name, torrentModel.infoHash)
    }

    override fun handleClicked(position: Int, torrentModel: TorrentModel) {
        controllerScope.launch {
            when (torrentModel.userState) {
                TorrentUserState.PAUSED -> {
                    // Change the cached version
                    torrentModel.userState = TorrentUserState.ACTIVE
                    torrentModel.downloadingState = TorrentDownloadingState.UNKNOWN
                    // Update the database
                    torrentDataSource.setTorrentState(
                        torrentModel.infoHash,
                        TorrentUserState.ACTIVE
                    )
                    subscriptionMediator.addTorrent(
                        this@TorrentsListController,
                        torrentModel.infoHash
                    )
                    // Requesting the service
                    torrentMediator.addTorrent(torrentModel.toIdentifier())
                    // notifying the view
                }
                TorrentUserState.ACTIVE -> {
                    torrentModel.userState = TorrentUserState.PAUSED
                    torrentDataSource.getTorrent(torrentModel.infoHash)?.let {
                        torrentModel.progress = it.progress
                        torrentModel.finished = it.isFinished
                    }
                    torrentDataSource.setTorrentState(
                        torrentModel.infoHash,
                        TorrentUserState.PAUSED
                    )
                    subscriptionMediator.removeTorrent(
                        this@TorrentsListController,
                        torrentModel.infoHash
                    )
                    if (!torrentMediator.removeTorrent(torrentModel.infoHash)) {
                        // no handler found so no request has been made
                    }
                }
            }
            viewMvc!!.notifyItemUpdate(torrentModel.infoHash)
        }
    }


}