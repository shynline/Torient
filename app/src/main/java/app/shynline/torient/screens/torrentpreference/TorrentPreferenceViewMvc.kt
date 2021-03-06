package app.shynline.torient.screens.torrentpreference

import app.shynline.torient.domain.database.entities.TorrentPreferenceSchema
import app.shynline.torient.screens.common.view.ObservableViewMvc

interface TorrentPreferenceViewMvc : ObservableViewMvc<TorrentPreferenceViewMvc.Listener> {
    interface Listener {
        fun onDownloadLimitChanged(rate: Int)
        fun onUploadLimitChanged(rate: Int)
        fun onHonorGlobalLimitChanged(checked: Boolean)
        fun onLimitDownloadRateChanged(checked: Boolean)
        fun onLimitUploadRateChanged(checked: Boolean)
    }

    fun updateUi(preferenceSchema: TorrentPreferenceSchema)
}