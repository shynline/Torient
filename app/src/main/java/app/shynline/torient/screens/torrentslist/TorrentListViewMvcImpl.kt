package app.shynline.torient.screens.torrentslist

import android.view.LayoutInflater
import android.view.ViewGroup
import app.shynline.torient.R
import app.shynline.torient.screens.common.view.BaseObservableViewMvc

class TorrentListViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?
) : BaseObservableViewMvc<TorrentListViewMvc.Listener>(), TorrentListViewMvc {

    init {
        inflater.inflate(R.layout.fragment_torrent_list_view, parent, false)
    }

}