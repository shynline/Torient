package app.shynline.torient.screens.torrentslist

import app.shynline.torient.database.TorrentDao
import app.shynline.torient.model.AddTorrentEvent
import app.shynline.torient.model.TorrentEvent
import app.shynline.torient.model.TorrentIdentifier
import app.shynline.torient.model.TorrentResumedEvent
import app.shynline.torient.screens.common.BaseController
import app.shynline.torient.screens.common.navigationhelper.PageNavigationHelper
import app.shynline.torient.screens.common.requesthelper.FragmentRequestHelper
import app.shynline.torient.screens.common.requesthelper.REQUEST_ID_OPEN_TORRENT_FILE
import app.shynline.torient.torrent.torrent.SubscriptionMediator
import app.shynline.torient.usecases.AddTorrentUseCase
import app.shynline.torient.usecases.GetTorrentDetailUseCase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class TorrentsListController(
    private val subscriptionMediator: SubscriptionMediator,
    private val getTorrentDetailUseCase: GetTorrentDetailUseCase,
    private val torrentDao: TorrentDao,
    private val addTorrentUseCase: AddTorrentUseCase
) : BaseController(), TorrentListViewMvc.Listener, SubscriptionMediator.Listener {

    private var viewMvc: TorrentListViewMvc? = null
    private var fragmentRequestHelper: FragmentRequestHelper? = null
    private var pageNavigationHelper: PageNavigationHelper? = null
    private val managedTorrents: MutableList<TorrentIdentifier> = mutableListOf()


    fun bind(
        viewMvc: TorrentListViewMvc, fragmentRequestHelper: FragmentRequestHelper,
        pageNavigationHelper: PageNavigationHelper
    ) {
        this.viewMvc = viewMvc
        this.fragmentRequestHelper = fragmentRequestHelper
        this.pageNavigationHelper = pageNavigationHelper
    }

    override fun onStatReceived(torrentEvent: TorrentEvent) {
        when (torrentEvent) {
            is AddTorrentEvent -> {
            }
            is TorrentResumedEvent -> {
            }
        }
    }

    fun unbind() {
        viewMvc = null
        fragmentRequestHelper = null
        pageNavigationHelper = null
    }

    override fun onStart() {
        viewMvc!!.registerListener(this)
        // Subscribe for previously being managed torrents
        subscriptionMediator.subscribe(this, managedTorrents.map { it.infoHash }.toTypedArray())
        queryDataBaseForChange()
    }

    override fun onStop() {
        subscriptionMediator.unsubscribe(this)
        viewMvc!!.unRegisterListener(this)
        super.onStop()
    }

    private fun queryDataBaseForChange() = controllerScope.launch {
        torrentDao.getTorrents().collect { torrentSchemas ->
            val removedTorrents = managedTorrents.toMutableList()
            torrentSchemas.forEach {
                val identifier = it.toIdentifier()
                if (!managedTorrents.contains(identifier)) {
                    managedTorrents.add(identifier)
                    subscriptionMediator.addTorrent(this@TorrentsListController, it.infoHash)
                    addTorrentUseCase.execute(identifier.magnet)
                } else {
                    removedTorrents.remove(identifier)
                }
            }
            // If any torrent were removed in other screens it appear here
            // They might be already removed from torrent service in that screen
            // It's torrent service's job to handle duplicate requests
            removedTorrents.forEach {
                removeTorrent(it)
            }
        }
    }

    private fun removeTorrent(identifier: TorrentIdentifier) {
        // send request
        // on response remove from managed torrent and ui
        // probably need a progress for removing?
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


}