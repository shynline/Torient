package app.shynline.torient.screens.preference

import app.shynline.torient.common.userpreference.UserPreference
import app.shynline.torient.screens.common.view.ObservableViewMvc

interface PreferenceViewMvc : ObservableViewMvc<PreferenceViewMvc.Listener> {

    interface Listener {
        fun onDownloadLimitChanged(rate: Int)
        fun onUploadLimitChanged(rate: Int)
        fun onMaximumPeerChanged(rate: Int)
        fun onLimitDownloadRateChanged(checked: Boolean)
        fun onLimitUploadRateChanged(checked: Boolean)
    }

    fun updateUi(userPreference: UserPreference)
}