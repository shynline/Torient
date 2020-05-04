package app.shynline.torient.screens.torrentslist

import app.shynline.torient.database.TorrentUserState
import app.shynline.torient.database.datasource.TorrentDataSource
import app.shynline.torient.model.TorrentDetail
import app.shynline.torient.screens.common.BaseController
import app.shynline.torient.screens.common.navigationhelper.PageNavigationHelper
import app.shynline.torient.screens.common.requesthelper.FragmentRequestHelper
import app.shynline.torient.screens.common.requesthelper.REQUEST_ID_OPEN_TORRENT_FILE
import app.shynline.torient.torrent.*
import app.shynline.torient.torrent.torrent.ManageState
import app.shynline.torient.torrent.torrent.TorrentDownloadingState
import app.shynline.torient.usecases.AddTorrentUseCase
import app.shynline.torient.usecases.GetAllManagedTorrentStatesUseCase
import app.shynline.torient.usecases.GetTorrentDetailUseCase
import app.shynline.torient.usecases.RemoveTorrentUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class TorrentsListController(
    private val subscriptionMediator: SubscriptionMediator,
    private val getTorrentDetailUseCase: GetTorrentDetailUseCase,
    private val torrentDataSource: TorrentDataSource,
    private val addTorrentUseCase: AddTorrentUseCase,
    private val getAllManagedTorrentStatesUseCase: GetAllManagedTorrentStatesUseCase,
    private val removeTorrentUseCase: RemoveTorrentUseCase
) : BaseController(), TorrentListViewMvc.Listener, SubscriptionMediator.Listener {

    private var viewMvc: TorrentListViewMvc? = null
    private var fragmentRequestHelper: FragmentRequestHelper? = null
    private var pageNavigationHelper: PageNavigationHelper? = null
    private val managedTorrents: MutableMap<String, TorrentDetail> = hashMapOf()

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
            when (torrentEvent) {
                is AddTorrentEvent -> {
                    changeManagedTorrentServiceState(torrentEvent.infoHash, ManageState.ADDED)
                }
                is TorrentResumedEvent -> {
                    changeManagedTorrentServiceState(torrentEvent.infoHash, ManageState.RESUMED)
                }
                is TorrentRemovedEvent -> {

                }
                is TorrentFinishedEvent -> {
                    torrentDataSource.setTorrentFinished(torrentEvent.infoHash, true)
                    changeManagedTorrentServiceState(torrentEvent.infoHash, ManageState.FINISHED)
                }
                is TorrentProgressEvent -> {
                    managedTorrents[torrentEvent.infoHash]?.let { torrent ->
                        when (torrentEvent.state) {
                            TorrentDownloadingState.UNKNOWN -> {
                            }
                            TorrentDownloadingState.ALLOCATING -> {
                                torrent.downloadingState = torrentEvent.state
                                torrent.progress = torrentEvent.progress
                                viewMvc!!.notifyItemUpdate(torrent.infoHash)
                            }
                            TorrentDownloadingState.CHECKING_FILES -> {
                                torrent.downloadingState = torrentEvent.state
                                torrent.progress = torrentEvent.progress
                                viewMvc!!.notifyItemUpdate(torrent.infoHash)
                            }
                            TorrentDownloadingState.CHECKING_RESUME_DATA -> {
                                torrent.downloadingState = torrentEvent.state
                                torrent.progress = torrentEvent.progress
                                viewMvc!!.notifyItemUpdate(torrent.infoHash)
                            }
                            TorrentDownloadingState.DOWNLOADING -> {
                                torrent.downloadingState = torrentEvent.state
                                torrent.downloadRate = torrentEvent.downloadRate
                                torrent.uploadRate = torrentEvent.uploadRate
                                torrent.progress = torrentEvent.progress
                                viewMvc!!.notifyItemUpdate(torrent.infoHash)
                                torrentDataSource.setTorrentProgress(
                                    torrentEvent.infoHash,
                                    torrentEvent.progress
                                )
                            }
                            TorrentDownloadingState.DOWNLOADING_METADATA -> {
                                torrent.downloadingState = torrentEvent.state
                                torrent.progress = torrentEvent.progress
                                viewMvc!!.notifyItemUpdate(torrent.infoHash)
                            }
                            TorrentDownloadingState.FINISHED -> {
                                torrent.downloadingState = torrentEvent.state
                                viewMvc!!.notifyItemUpdate(torrent.infoHash)
                                torrentDataSource.setTorrentFinished(torrentEvent.infoHash, true)
                            }
                            TorrentDownloadingState.SEEDING -> {
                                torrent.downloadingState = torrentEvent.state
                                torrent.downloadRate = torrentEvent.downloadRate
                                torrent.uploadRate = torrentEvent.uploadRate
                                viewMvc!!.notifyItemUpdate(torrent.infoHash)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun changeManagedTorrentServiceState(infoHash: String, state: ManageState) {
        managedTorrents[infoHash]?.let {
            // Change the state of the torrent detail
            it.serviceState = state
            // notify the view to update accordingly
            viewMvc!!.notifyItemUpdate(it.infoHash)
        }
    }

    fun unbind() {
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
        subscriptionMediator.unsubscribe(this)
        viewMvc!!.unRegisterListener(this)
        queryingDataBaseJob?.cancel()
    }

    private fun queryDataBaseForChange() = controllerScope.launch {
        torrentDataSource.getTorrents().collect { torrentSchemas ->
            // The Torrents which are being managed by the service so we shouldn't add them again
            val serviceManagedTorrents = getAllManagedTorrentStatesUseCase.execute()
            // It's a flow so it will be called on each database transaction
            // Make a copy of our managed torrent to find out which torrents are not in database any more
            val removedTorrents = managedTorrents.keys.toMutableList()
            // Traversing through torrents in database
            torrentSchemas.forEach {
                if (!managedTorrents.containsKey(it.infoHash)) {
                    // We add it to our managed list
                    managedTorrents[it.infoHash] = getTorrentDetailUseCase.execute(
                        infoHash = it.infoHash
                    )!!.apply {
                        userState = it.userState
                        progress = it.progress
                        finished = it.isFinished
                    }

                    // Check if torrent is not in our managed cache
                    if (!serviceManagedTorrents.containsKey(it.infoHash)) {
                        // The torrent is not being manage by the service
                        // If user wants it active
                        // We notify service for adding it
                        if (it.userState == TorrentUserState.ACTIVE) {
                            // Subscribe to the service for this torrent
                            subscriptionMediator.addTorrent(
                                this@TorrentsListController,
                                it.infoHash
                            )
                            addTorrentUseCase.execute(it.toIdentifier())
                        }
                    } else {
                        // If the torrent is managed by service
                        // but it's not suppose to be active
                        // We remove it from the service
                        if (it.userState == TorrentUserState.PAUSED) {
                            removeTorrentUseCase.execute(it.infoHash)
                            subscriptionMediator.removeTorrent(
                                this@TorrentsListController,
                                it.infoHash
                            )
                        }
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
                    if (!serviceManagedTorrents.containsKey(it.infoHash)) {
                        // The torrent is not being manage by the service
                        // If user wants it active
                        // We notify service for adding it
                        if (it.userState == TorrentUserState.ACTIVE) {
                            // Subscribe to the service for this torrent
                            subscriptionMediator.addTorrent(
                                this@TorrentsListController,
                                it.infoHash
                            )
                            addTorrentUseCase.execute(it.toIdentifier())
                        }
                    } else {
                        // If the torrent is managed by service
                        // but it's not suppose to be active
                        // We remove it from the service
                        if (it.userState == TorrentUserState.PAUSED) {
                            removeTorrentUseCase.execute(it.infoHash)
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
                if (serviceManagedTorrents.containsKey(it)) {
                    // The service is managing the torrent
                    // Request to remove it
                    removeTorrentUseCase.execute(it)
                }
            }

            viewMvc!!.showTorrents(managedTorrents.values.toList())
        }
    }


    override fun addTorrentFile() {
        fragmentRequestHelper!!.openTorrentFile(REQUEST_ID_OPEN_TORRENT_FILE)
    }

    fun openTorrentFile(torrentData: ByteArray) = controllerScope.launch {
        val torrentDetail = getTorrentDetailUseCase.execute(torrentFile = torrentData)
        if (torrentDetail != null) {
            pageNavigationHelper!!.showNewTorrentDialog(torrentDetail.infoHash)
        } else {
            // TODO notify user
        }

    }

    override fun handleClicked(position: Int, torrentDetail: TorrentDetail) {
        controllerScope.launch {
            when (torrentDetail.userState) {
                TorrentUserState.PAUSED -> {
                    // Change the cached version
                    torrentDetail.userState = TorrentUserState.ACTIVE
                    // Update the database
                    torrentDataSource.setTorrentState(
                        torrentDetail.infoHash,
                        TorrentUserState.ACTIVE
                    )
                    subscriptionMediator.addTorrent(
                        this@TorrentsListController,
                        torrentDetail.infoHash
                    )
                    // Requesting the service
                    addTorrentUseCase.execute(torrentDetail.toIdentifier())
                    // notifying the view
                    viewMvc!!.notifyItemUpdate(torrentDetail.infoHash)
                }
                TorrentUserState.ACTIVE -> {
                    torrentDetail.userState = TorrentUserState.PAUSED
                    torrentDataSource.getTorrent(torrentDetail.infoHash)?.let {
                        torrentDetail.progress = it.progress
                        torrentDetail.finished = it.isFinished
                    }
                    torrentDataSource.setTorrentState(
                        torrentDetail.infoHash,
                        TorrentUserState.PAUSED
                    )
                    subscriptionMediator.removeTorrent(
                        this@TorrentsListController,
                        torrentDetail.infoHash
                    )
                    if (!removeTorrentUseCase.execute(torrentDetail.infoHash)) {
                        // no handler found so no request has been made
                    }
                    viewMvc!!.notifyItemUpdate(torrentDetail.infoHash)
                }
            }
        }
    }


}