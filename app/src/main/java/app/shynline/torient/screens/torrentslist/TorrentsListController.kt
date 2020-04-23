package app.shynline.torient.screens.torrentslist

import app.shynline.torient.screens.common.requesthelper.FragmentRequestHelper

class TorrentsListController : TorrentListViewMvc.Listener {

    private lateinit var viewMvc: TorrentListViewMvc
    private lateinit var fragmentRequestHelper: FragmentRequestHelper


    fun bind(viewMvc: TorrentListViewMvc, fragmentRequestHelper: FragmentRequestHelper) {
        this.viewMvc = viewMvc
        this.fragmentRequestHelper = fragmentRequestHelper
    }

    fun onStart() {
        viewMvc.registerListener(this)
    }

    fun onStop() {
        viewMvc.unRegisterListener(this)
    }

    override fun addTorrentFile() {
        fragmentRequestHelper.openTorrentFile(REQUEST_ID_OPEN_TORRENT_FILE)
    }

    fun openTorrentFile(torrentData: ByteArray) {

    }


}