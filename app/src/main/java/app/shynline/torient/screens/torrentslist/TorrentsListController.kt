package app.shynline.torient.screens.torrentslist

import app.shynline.torient.database.common.states.TorrentUserState
import app.shynline.torient.database.datasource.torrent.TorrentDataSource
import app.shynline.torient.database.datasource.torrentfilepriority.TorrentFilePriorityDataSource
import app.shynline.torient.model.TorrentModel
import app.shynline.torient.screens.common.BaseController
import app.shynline.torient.screens.common.navigationhelper.PageNavigationHelper
import app.shynline.torient.screens.common.requesthelper.FragmentRequestHelper
import app.shynline.torient.screens.common.requesthelper.REQUEST_ID_OPEN_TORRENT_FILE
import app.shynline.torient.torrent.events.TorrentEvent
import app.shynline.torient.torrent.events.TorrentMetaDataEvent
import app.shynline.torient.torrent.events.TorrentProgressEvent
import app.shynline.torient.torrent.mediator.SubscriptionMediator
import app.shynline.torient.torrent.mediator.TorrentMediator
import app.shynline.torient.torrent.states.TorrentDownloadingState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class TorrentsListController(
    private val subscriptionMediator: SubscriptionMediator,
    private val torrentDataSource: TorrentDataSource,
    private val torrentMediator: TorrentMediator,
    private val torrentFilePriorityDataSource: TorrentFilePriorityDataSource
) : BaseController(), TorrentListViewMvc.Listener, SubscriptionMediator.Listener {

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

    override fun onStatReceived(torrentEvent: TorrentEvent) {
        controllerScope.launch {
            // View Mvc might be null here
            // In case of calling onStatReceived right before destroying view
            // because this coroutine is called and it might be still active( if the app is open and
            // onDestroyView was called due to screen change )
            // the rest of the coroutine processes right after clearing viewMvc
            // It's a rare case and It's safe
            // That's why I used kotlin null safety for updating the view
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
                                calculateProgressData(torrent, torrentEvent.fileProgress)
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
                        calculateProgressData(it, schema.fileProgress)
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

    override fun unbind() {
        viewMvc = null
        fragmentRequestHelper = null
        pageNavigationHelper = null
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
                if (!managedTorrents.containsKey(it.infoHash)) {
                    val torrentDetail = torrentMediator.getTorrentModel(
                        infoHash = it.infoHash
                    ) ?: TorrentModel(
                        infoHash = it.infoHash,
                        name = it.name,
                        magnet = it.magnet
                    )
                    // We add it to our managed list
                    managedTorrents[it.infoHash] = torrentDetail.apply {
                        userState = it.userState
                        progress = it.progress
                        finished = it.isFinished
                    }

                    if (it.userState == TorrentUserState.ACTIVE) {
                        // Subscribe to the service for this torrent
                        subscriptionMediator.addTorrent(
                            this@TorrentsListController,
                            it.infoHash
                        )
                        torrentMediator.addTorrent(it.toIdentifier())
                    } else if (it.userState == TorrentUserState.PAUSED) {
                        torrentMediator.removeTorrent(it.infoHash)
                        subscriptionMediator.removeTorrent(
                            this@TorrentsListController,
                            it.infoHash
                        )
                    }
                } else {
                    // We have the torrent so we remove it from our removedTorrent
                    removedTorrents.remove(it.infoHash)
                    // Update state if we have it in our managed cache
                    managedTorrents[it.infoHash]!!.apply {
                        userState = it.userState
                        finished = it.isFinished
                        progress = it.progress
                    }

                    // Check if torrent is not in our managed cache
                    if (!serviceManagedTorrents.contains(it.infoHash)) {
                        // The torrent is not being manage by the service
                        // If user wants it active
                        // We notify service for adding it
                        if (it.userState == TorrentUserState.ACTIVE) {
                            // Subscribe to the service for this torrent
                            subscriptionMediator.addTorrent(
                                this@TorrentsListController,
                                it.infoHash
                            )
                            torrentMediator.addTorrent(it.toIdentifier())
                        }
                    } else {
                        // If the torrent is managed by service
                        // but it's not suppose to be active
                        // We remove it from the service
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
            removedTorrents.forEach {
                managedTorrents.remove(it)
                if (serviceManagedTorrents.contains(it)) {
                    // The service is managing the torrent
                    // Request to remove it
                    torrentMediator.removeTorrent(it)
                }
            }
            managedTorrents.values.forEach { torrentModel ->
                torrentModel.filePriority =
                    torrentFilePriorityDataSource.getPriority(torrentModel.infoHash).filePriority
                calculateProgressData(torrentModel, torrentFilesProgress[torrentModel.infoHash])
            }
            viewMvc!!.showTorrents(managedTorrents.values.toList())
        }
    }

    private fun calculateProgressData(
        torrentModel: TorrentModel,
        fileProgress: List<Long>?
    ) {
        val filePriority = torrentModel.filePriority
        if (torrentModel.torrentFile == null || filePriority == null) { // Meta data is not available
            torrentModel.selectedFilesBytesDone = 0f
            torrentModel.selectedFilesSize = torrentModel.totalSize
            return
        }
        var selectedBytesDone = 0f
        var selectedSize = 0L
        filePriority.forEachIndexed { index, torrentFilePriority ->
            if (torrentFilePriority.active) {
                selectedSize += torrentModel.filesSize!![index]
                selectedBytesDone += fileProgress?.get(index) ?: 0
            }
        }
        torrentModel.selectedFilesSize = selectedSize
        torrentModel.selectedFilesBytesDone = selectedBytesDone
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
                    viewMvc!!.notifyItemUpdate(torrentModel.infoHash)
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
                    viewMvc!!.notifyItemUpdate(torrentModel.infoHash)
                }
            }
        }
    }


}