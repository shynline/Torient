package app.shynline.torient.screens.torrentfiles

import android.view.LayoutInflater
import android.view.ViewGroup
import app.shynline.torient.R
import app.shynline.torient.screens.common.view.BaseObservableViewMvc

class TorrentFilesViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?
) : BaseObservableViewMvc<TorrentFilesViewMvc.Listener>(), TorrentFilesViewMvc {

    init {
        setRootView(inflater.inflate(R.layout.fragment_torrent_files, parent, false))
    }
}