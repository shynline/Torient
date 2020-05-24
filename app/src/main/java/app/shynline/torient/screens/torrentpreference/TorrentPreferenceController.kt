package app.shynline.torient.screens.torrentpreference

import app.shynline.torient.database.datasource.torrentpreference.TorrentPreferenceDataSource
import app.shynline.torient.database.entities.TorrentPreferenceSchema
import app.shynline.torient.screens.common.BaseController
import app.shynline.torient.torrent.mediator.TorrentMediator
import kotlinx.coroutines.launch

class TorrentPreferenceController(
    private val torrentMediator: TorrentMediator,
    private val torrentPreferenceDataSource: TorrentPreferenceDataSource
) : BaseController(), TorrentPreferenceViewMvc.Listener {

    private var viewMvc: TorrentPreferenceViewMvc? = null
    private lateinit var infoHash: String
    private lateinit var torrentPreference: TorrentPreferenceSchema
    private var preferenceHash: Int = 0

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
        retrievePreference()
    }

    override fun onStop() {
        viewMvc!!.unRegisterListener(this)
        savePreference()
    }

    fun setTorrent(infoHash: String) {
        this.infoHash = infoHash
    }

    private fun retrievePreference() = controllerScope.launch {
        torrentPreference = torrentPreferenceDataSource.getTorrentPreference(infoHash)
        preferenceHash = torrentPreference.hashCode()
        viewMvc!!.updateUi(torrentPreference)
    }

    private fun savePreference() {
        if (torrentPreference.hashCode() == preferenceHash)
            return
        torrentPreferenceDataSource.updateTorrentPreference(torrentPreference)
        torrentMediator.updateTorrentPreference(infoHash)
    }
}