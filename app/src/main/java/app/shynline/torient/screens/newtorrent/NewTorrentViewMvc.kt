package app.shynline.torient.screens.newtorrent

import app.shynline.torient.model.TorrentModel
import app.shynline.torient.screens.common.view.ObservableViewMvc

interface NewTorrentViewMvc : ObservableViewMvc<NewTorrentViewMvc.Listener> {
    interface Listener {
        fun downloadTorrent()
        fun addTorrent()
    }

    fun showTorrent(torrentModel: TorrentModel)

}