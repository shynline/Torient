package app.shynline.torient.screens.torrentoverview

import app.shynline.torient.database.datasource.TorrentDataSource
import app.shynline.torient.database.states.TorrentUserState
import app.shynline.torient.screens.common.BaseController
import app.shynline.torient.torrent.mediator.TorrentMediator
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.fixedRateTimer

class TorrentOverviewController(
    private val torrentMediator: TorrentMediator,
    private val torrentDataSource: TorrentDataSource
) : BaseController(), TorrentOverviewViewMvc.Listener {

    private var viewMvc: TorrentOverviewViewMvc? = null
    private lateinit var infoHash: String
    private var periodicTimer: Timer? = null

    fun bind(viewMvc: TorrentOverviewViewMvc) {
        this.viewMvc = viewMvc
    }

    fun unbind() {
        viewMvc = null
    }

    override fun onStart() {
        viewMvc!!.registerListener(this)
        periodicTimer = fixedRateTimer(
            name = "periodicTaskTorrentOverview",
            initialDelay = 1000,
            period = 1000
        ) { periodicTask() }
    }

    override fun onStop() {
        viewMvc!!.unRegisterListener(this)
        periodicTimer?.cancel()
    }

    fun setTorrent(infoHash: String) {
        this.infoHash = infoHash
        controllerScope.launch {
            update()
        }
    }

    private suspend fun update() {
        // If it returns null this torrent doesn't have meta data (added by magnet)
        torrentMediator.torrentOverview(infoHash)?.let { torrentOverview ->
            if (torrentOverview.userState != TorrentUserState.ACTIVE) {
                torrentDataSource.getTorrent(infoHash)?.let { torrentSchema ->
                    torrentOverview.progress = if (torrentSchema.isFinished) {
                        1f
                    } else {
                        torrentSchema.progress
                    }
                }
            }
            viewMvc!!.updateUi(torrentOverview)
        }
    }

    private fun periodicTask() = controllerScope.launch {
        update()
    }


}