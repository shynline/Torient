package app.shynline.torient.screens.torrentoverview

import app.shynline.torient.screens.common.BaseController

class TorrentOverviewController : BaseController(), TorrentOverviewViewMvc.Listener {

    private var viewMvc: TorrentOverviewViewMvc? = null

    fun bind(viewMvc: TorrentOverviewViewMvc) {
        this.viewMvc = viewMvc
    }

    fun unbind() {
        viewMvc = null
    }

    override fun onStart() {
        viewMvc!!.registerListener(this)
    }

    override fun onStop() {
        viewMvc!!.unRegisterListener(this)
    }

    fun setTorrent(infoHash: String) {

    }
}