package app.shynline.torient.screens.newtorrent

import app.shynline.torient.model.TorrentDetail
import app.shynline.torient.screens.common.view.ObservableViewMvc

interface NewTorrentViewMvc : ObservableViewMvc<NewTorrentViewMvc.Listener> {
    interface Listener {

    }

    fun showTorrent(torrentDetail: TorrentDetail)

}