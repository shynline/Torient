package app.shynline.torient.screens.torrentslist

import app.shynline.torient.model.TorrentModel
import app.shynline.torient.screens.common.view.ObservableViewMvc

interface TorrentListViewMvc : ObservableViewMvc<TorrentListViewMvc.Listener> {
    interface Listener {
        fun addTorrentFile()
        fun addTorrentMagnet()
        fun handleClicked(position: Int, torrentModel: TorrentModel)
        fun onSaveToDownloadRequested(torrentModel: TorrentModel)
        fun onRemoveTorrent(torrentModel: TorrentModel)
        fun onCopyMagnetRequested(torrentModel: TorrentModel)
    }

    fun showTorrents(torrentModels: List<TorrentModel>)
    fun notifyItemUpdate(infoHash: String)
    fun removeTorrent(identifier: Long)

}