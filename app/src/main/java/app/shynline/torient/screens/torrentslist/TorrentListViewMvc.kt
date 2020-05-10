package app.shynline.torient.screens.torrentslist

import app.shynline.torient.model.TorrentDetail
import app.shynline.torient.screens.common.view.ObservableViewMvc

interface TorrentListViewMvc : ObservableViewMvc<TorrentListViewMvc.Listener> {
    interface Listener {
        fun addTorrentFile()
        fun addTorrentMagnet()
        fun handleClicked(position: Int, torrentDetail: TorrentDetail)
        fun onSaveToDownloadRequested(torrentDetail: TorrentDetail)
    }

    fun showTorrents(torrentDetails: List<TorrentDetail>)
    fun notifyItemUpdate(infoHash: String)
    fun removeTorrent(identifier: Long)

}