package app.shynline.torient.screens.torrentslist

import app.shynline.torient.screens.common.view.ObservableViewMvc

interface TorrentListViewMvc : ObservableViewMvc<TorrentListViewMvc.Listener> {
    interface Listener {
        fun addTorrentFile()
    }

}