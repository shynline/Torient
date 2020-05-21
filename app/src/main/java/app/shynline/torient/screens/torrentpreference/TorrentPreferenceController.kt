package app.shynline.torient.screens.torrentpreference

import app.shynline.torient.screens.common.BaseController

class TorrentPreferenceController : BaseController(), TorrentPreferenceViewMvc.Listener {

    private var viewMvc: TorrentPreferenceViewMvc? = null

    fun bind(viewMvc: TorrentPreferenceViewMvc) {
        this.viewMvc = viewMvc
    }


    override fun loadState(state: HashMap<String, Any>?) {
    }

    override fun saveState(): HashMap<String, Any>? {
        return null
    }

    override fun unbind() {
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