package app.shynline.torient.screens.torrentfiles

import app.shynline.torient.screens.common.BaseController

class TorrentFilesController : BaseController(), TorrentFilesViewMvc.Listener {

    private var viewMvc: TorrentFilesViewMvc? = null

    fun bind(viewMvc: TorrentFilesViewMvc) {
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