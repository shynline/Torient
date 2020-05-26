package app.shynline.torient.screens.torrentpreference

import app.shynline.torient.database.datasource.torrentpreference.TorrentPreferenceDataSource
import app.shynline.torient.database.entities.TorrentPreferenceSchema
import app.shynline.torient.screens.common.BaseController
import app.shynline.torient.torrent.mediator.TorrentMediator
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.fixedRateTimer

class TorrentPreferenceController(
    private val torrentMediator: TorrentMediator,
    private val torrentPreferenceDataSource: TorrentPreferenceDataSource
) : BaseController(), TorrentPreferenceViewMvc.Listener {

    private var viewMvc: TorrentPreferenceViewMvc? = null
    private lateinit var infoHash: String
    private var torrentPreference: TorrentPreferenceSchema? = null
    private var preferenceHash: Int = 0
    private var periodicTimer: Timer? = null

    fun bind(viewMvc: TorrentPreferenceViewMvc) {
        this.viewMvc = viewMvc
    }


    override fun loadState(state: HashMap<String, Any>?) {
    }

    override fun saveState(): HashMap<String, Any>? {
        return null
    }

    override fun unbind() {
        viewMvc = null
    }

    override fun onStart() {
        viewMvc!!.registerListener(this)
        viewMvc!!.addListeners()
        retrievePreference()
        periodicTimer = fixedRateTimer(
            name = "periodicTaskTorrentPreference",
            initialDelay = 500,
            period = 500
        ) { savePreference() }
    }

    override fun onStop() {
        viewMvc!!.removeListeners()
        viewMvc!!.unRegisterListener(this)
        periodicTimer?.cancel()
        controllerScope.launch {
            savePreference()
        }
    }

    fun setTorrent(infoHash: String) {
        this.infoHash = infoHash
    }

    private fun retrievePreference() = controllerScope.launch {
        torrentPreference = torrentPreferenceDataSource.getTorrentPreference(infoHash)
        preferenceHash = torrentPreference.hashCode()
        viewMvc!!.updateUi(torrentPreference!!)
    }

    private fun savePreference() = controllerScope.launch {
        if (torrentPreference == null || torrentPreference.hashCode() == preferenceHash)
            return@launch
        torrentPreferenceDataSource.updateTorrentPreference(torrentPreference!!)
        torrentMediator.updateTorrentPreference(infoHash)
    }

    override fun onDownloadLimitChanged(rate: Int) {
        torrentPreference!!.downloadRate = rate
    }

    override fun onUploadLimitChanged(rate: Int) {
        torrentPreference!!.uploadRate = rate
    }

    override fun onHonorGlobalLimitChanged(checked: Boolean) {
        torrentPreference!!.honorGlobalRate = checked
    }

    override fun onLimitDownloadRateChanged(checked: Boolean) {
        torrentPreference!!.downloadRateLimit = checked
    }

    override fun onLimitUploadRateChanged(checked: Boolean) {
        torrentPreference!!.uploadRateLimit = checked
    }
}