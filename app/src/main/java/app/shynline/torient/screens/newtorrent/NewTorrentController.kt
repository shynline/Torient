package app.shynline.torient.screens.newtorrent

import app.shynline.torient.screens.common.navigationhelper.PageNavigationHelper
import app.shynline.torient.usecases.GetTorrentDetailUseCase

class NewTorrentController(
    private val getTorrentDetailUseCase: GetTorrentDetailUseCase
) : NewTorrentViewMvc.Listener {
    private var viewMvc: NewTorrentViewMvc? = null
    private var pageNavigationHelper: PageNavigationHelper? = null


    fun bind(
        viewMvc: NewTorrentViewMvc,
        pageNavigationHelper: PageNavigationHelper
    ) {
        this.viewMvc = viewMvc
        this.pageNavigationHelper = pageNavigationHelper
    }

    fun showTorrent(infoHash: String) {
        val torrentDetail = getTorrentDetailUseCase.execute(infoHash = infoHash)
        if (torrentDetail != null) {
            viewMvc!!.showTorrent(torrentDetail)
        } else {
            close()
        }
    }

    fun onStart() {

    }

    fun onStop() {

    }

    private fun close() {
        pageNavigationHelper!!.back()
    }

    fun unbind() {
        viewMvc = null
        pageNavigationHelper = null
    }

}