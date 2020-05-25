package app.shynline.torient.screens.preference

import app.shynline.torient.common.userpreference.UserPreference
import app.shynline.torient.screens.common.BaseController
import app.shynline.torient.torrent.mediator.TorrentMediator

class PreferenceController(
    private val userPreference: UserPreference,
    private val torrentMediator: TorrentMediator
) : BaseController(), PreferenceViewMvc.Listener {

    private var viewMvc: PreferenceViewMvc? = null

    override fun saveState(): HashMap<String, Any>? {
        return null
    }

    override fun loadState(state: HashMap<String, Any>?) {
    }

    fun bind(viewMvc: PreferenceViewMvc) {
        this.viewMvc = viewMvc
    }

    override fun unbind() {
        viewMvc = null
    }

    override fun onStart() {
        viewMvc!!.registerListener(this)
        viewMvc!!.addListeners()
        loadPreference()
    }

    override fun onStop() {
        viewMvc!!.unRegisterListener(this)
        viewMvc!!.removeListeners()
        torrentMediator.onUpdateGlobalPreference()
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