package app.shynline.torient.screens.torrentslist

import app.shynline.torient.model.TorrentDetail
import app.shynline.torient.screens.common.view.ObservableViewMvc

interface TorrentListViewMvc : ObservableViewMvc<TorrentListViewMvc.Listener> {
    interface Listener {
        fun addTorrentFile()
        fun handleClicked(position: Int, torrentDetail: TorrentDetail)
    }

    fun showTorrents(torrentDetails: List<TorrentDetail>)
    fun notifyItemChange(position: Int)
    fun notifyItemChangeIdentifier(identifier: Long)
    fun removeTorrent(identifier: Long)

}