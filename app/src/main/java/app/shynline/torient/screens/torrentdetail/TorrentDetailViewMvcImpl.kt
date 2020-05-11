package app.shynline.torient.screens.torrentdetail

import android.view.LayoutInflater
import android.view.ViewGroup
import app.shynline.torient.R
import app.shynline.torient.screens.common.view.BaseObservableViewMvc

class TorrentDetailViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?
) : BaseObservableViewMvc<TorrentDetailViewMvc.Listener>(),
    TorrentDetailViewMvc {

    init {
        setRootView(inflater.inflate(R.layout.fragment_torrent_detail, parent, false))

    }
}