package app.shynline.torient.screens.torrentoverview

import app.shynline.torient.domain.models.TorrentOverview
import app.shynline.torient.screens.common.view.ObservableViewMvc

interface TorrentOverviewViewMvc : ObservableViewMvc<TorrentOverviewViewMvc.Listener> {

    interface Listener {

    }

    fun updateUi(torrentOverview: TorrentOverview)
}