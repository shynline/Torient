package app.shynline.torient.screens.torrentdetail

import app.shynline.torient.database.datasource.TorrentDataSource
import app.shynline.torient.screens.common.BaseController
import app.shynline.torient.screens.common.navigationhelper.PageNavigationHelper
import app.shynline.torient.torrent.mediator.SubscriptionMediator
import app.shynline.torient.torrent.mediator.TorrentMediator

class TorrentDetailController(
    private val subscriptionMediator: SubscriptionMediator,
    private val torrentDataSource: TorrentDataSource,
    private val torrentMediator: TorrentMediator
) : BaseController(), TorrentDetailViewMvc.Listener {

    private var viewMvc: TorrentDetailViewMvc? = null
    private var pageNavigationHelper: PageNavigationHelper? = null

    override fun onStart() {
        viewMvc!!.registerListener(this)
    }

    override fun onStop() {
        viewMvc!!.unRegisterListener(this)
    }

    fun bind(viewMvc: TorrentDetailViewMvc, pageNavigationHelper: PageNavigationHelper) {
        this.viewMvc = viewMvc
        this.pageNavigationHelper = pageNavigationHelper
    }

    fun unbind() {
        viewMvc = null
        pageNavigationHelper = null
    }

}