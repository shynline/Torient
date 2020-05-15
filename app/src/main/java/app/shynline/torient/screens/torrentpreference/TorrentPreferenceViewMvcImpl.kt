package app.shynline.torient.screens.torrentpreference

import android.view.LayoutInflater
import android.view.ViewGroup
import app.shynline.torient.R
import app.shynline.torient.screens.common.view.BaseObservableViewMvc

class TorrentPreferenceViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?
) : BaseObservableViewMvc<TorrentPreferenceViewMvc.Listener>(), TorrentPreferenceViewMvc {

    init {
        setRootView(inflater.inflate(R.layout.fragment_torrent_preference, parent, false))
    }
}