package app.shynline.torient.screens.preference

import app.shynline.torient.domain.torrentmanager.torrent.Torrent
import app.shynline.torient.domain.userpreference.UserPreference
import app.shynline.torient.screens.common.BaseController
import kotlinx.coroutines.CoroutineDispatcher

class PreferenceController(
    coroutineDispatcher: CoroutineDispatcher,
    private val userPreference: UserPreference,
    private val torrent: Torrent
) : BaseController(coroutineDispatcher), PreferenceViewMvc.Listener {

    private var viewMvc: PreferenceViewMvc? = null

    override fun saveState(): HashMap<String, Any>? {
        return null
    }

    override fun loadState(state: HashMap<String, Any>?) {
    }

    fun bind(viewMvc: PreferenceViewMvc) {
        this.viewMvc = viewMvc
    }

    override fun cleanUp() {
        viewMvc = null
    }

    override fun onStart() {
        viewMvc!!.registerListener(this)
        loadPreference()
    }

    override fun onStop() {
        viewMvc!!.unRegisterListener(this)
        torrent.onUpdateGlobalPreference()
    }

    private fun loadPreference() {
        viewMvc!!.updateUi(userPreference)
    }

    override fun onDownloadLimitChanged(rate: Int) {
        userPreference.globalDownloadRate = rate
    }

    override fun onUploadLimitChanged(rate: Int) {
        userPreference.globalUploadRate = rate
    }

    override fun onMaximumPeerChanged(rate: Int) {
        userPreference.globalMaxConnection = rate
    }

    override fun onLimitDownloadRateChanged(checked: Boolean) {
        userPreference.globalDownloadRateLimit = checked
    }

    override fun onLimitUploadRateChanged(checked: Boolean) {
        userPreference.globalUploadRateLimit = checked
    }
}