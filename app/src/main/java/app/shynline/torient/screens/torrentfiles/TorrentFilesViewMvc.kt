package app.shynline.torient.screens.torrentfiles

import app.shynline.torient.model.TorrentFilePriority
import app.shynline.torient.model.TorrentModel
import app.shynline.torient.screens.common.view.ObservableViewMvc

interface TorrentFilesViewMvc : ObservableViewMvc<TorrentFilesViewMvc.Listener> {

    interface Listener {
        fun onPriorityClicked(index: Int)
        fun onDownloadCheckBoxClicked(index: Int, download: Boolean)
    }

    fun showTorrent(torrentModel: TorrentModel)
    fun updateFileProgress(fileProgress: List<Long>)
    fun updateFilePriority(torrentFilePriorities: List<TorrentFilePriority>)
    fun saveState(): HashMap<String, Any>
    fun loadState(state: HashMap<String, Any>?)
}