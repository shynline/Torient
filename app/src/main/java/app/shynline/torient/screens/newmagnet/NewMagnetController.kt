package app.shynline.torient.screens.newmagnet

import app.shynline.torient.database.datasource.TorrentDataSource
import app.shynline.torient.screens.common.BaseController
import app.shynline.torient.screens.common.navigationhelper.PageNavigationHelper
import app.shynline.torient.torrent.mediator.TorrentMediator

class NewMagnetController(
    private val torrentMediator: TorrentMediator,
    private val torrentDataSource: TorrentDataSource
) : BaseController(), NewMagnetViewMvc.Listener {

    private var viewMvc: NewMagnetViewMvc? = null
    private var pageNavigationHelper: PageNavigationHelper? = null

    fun bind(
        viewMvc: NewMagnetViewMvc,
        pageNavigationHelper: PageNavigationHelper
    ) {
        this.viewMvc = viewMvc
        this.pageNavigationHelper = pageNavigationHelper
    }


    override fun onStart() {
        viewMvc!!.registerListener(this)
    }

    override fun onStop() {
        viewMvc!!.unRegisterListener(this)
    }

    fun unbind() {
        viewMvc = null
        pageNavigationHelper = null
    }

    fun showTorrent(magnet: String) {

    }
}