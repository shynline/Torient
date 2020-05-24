package app.shynline.torient.screens.torrentpreference

import app.shynline.torient.database.entities.TorrentPreferenceSchema
import app.shynline.torient.screens.common.view.ObservableViewMvc

interface TorrentPreferenceViewMvc : ObservableViewMvc<TorrentPreferenceViewMvc.Listener> {
    interface Listener {

    }

    fun updateUi(preferenceSchema: TorrentPreferenceSchema)
}