package app.shynline.torient.screens.torrentoverview

import android.view.LayoutInflater
import android.view.ViewGroup
import app.shynline.torient.R
import app.shynline.torient.screens.common.view.BaseObservableViewMvc

class TorrentOverviewViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?
) : BaseObservableViewMvc<TorrentOverviewViewMvc.Listener>(), TorrentOverviewViewMvc {

    init {
        setRootView(inflater.inflate(R.layout.fragment_torrent_overview, parent, false))
    }
}