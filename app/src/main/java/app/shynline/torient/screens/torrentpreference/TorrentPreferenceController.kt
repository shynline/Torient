package app.shynline.torient.screens.torrentpreference

import app.shynline.torient.database.datasource.torrentpreference.TorrentPreferenceDataSource
import app.shynline.torient.database.entities.TorrentPreferenceSchema
import app.shynline.torient.domain.helper.timer.TimerController
import app.shynline.torient.screens.common.BaseController
import app.shynline.torient.torrent.torrent.Torrent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.*

class TorrentPreferenceController(
    coroutineDispatcher: CoroutineDispatcher,
    private val torrent: Torrent,
    private val torrentPreferenceDataSource: TorrentPreferenceDataSource,
    private val timerController: TimerController
) : BaseController(coroutineDispatcher), TorrentPreferenceViewMvc.Listener {

    private lateinit var viewMvc: TorrentPreferenceViewMvc
    private lateinit var infoHash: String
    private lateinit var torrentPreference: TorrentPreferenceSchema
    private var preferenceHash: Int = 0
    private var preferenceLoaded = false

    fun bind(viewMvc: TorrentPreferenceViewMvc) {
        this.viewMvc = viewMvc
    }


    override fun loadState(state: HashMap<String, Any>?) {
    }

    override fun saveState(): HashMap<String, Any>? {
        return null
    }

    override fun onStart() {
        viewMvc.registerListener(this)
        retrievePreference()
        timerController.schedule(this, 500, 500) {
            savePreference()
        }
    }

    override fun onStop() {
        viewMvc.unRegisterListener(this)
        controllerScope.launch {
            savePreference()
        }
        timerController.cancel(this)
    }

    fun setTorrent(infoHash: String) {
        this.infoHash = infoHash
    }

    private fun retrievePreference() = controllerScope.launch {
        torrentPreference = torrentPreferenceDataSource.getTorrentPreference(infoHash)
        preferenceHash = torrentPreference.hashCode()
        viewMvc.updateUi(torrentPreference)
        preferenceLoaded = true
    }

    private fun savePreference() = controllerScope.launch {
        // Preference has not loaded yet
        if (!preferenceLoaded)
            return@launch
        // No change has been made to preference
        if (torrentPreference.hashCode() == preferenceHash) {
            return@launch
        }
        torrentPreferenceDataSource.updateTorrentPreference(torrentPreference)
        torrent.updateTorrentPreference(infoHash)
    }

    override fun onDownloadLimitChanged(rate: Int) {
        torrentPreference.downloadRate = rate
    }

    override fun onUploadLimitChanged(rate: Int) {
        torrentPreference.uploadRate = rate
    }

    override fun onHonorGlobalLimitChanged(checked: Boolean) {
        torrentPreference.honorGlobalRate = checked
    }

    override fun onLimitDownloadRateChanged(checked: Boolean) {
        torrentPreference.downloadRateLimit = checked
    }

    override fun onLimitUploadRateChanged(checked: Boolean) {
        torrentPreference.uploadRateLimit = checked
    }
}