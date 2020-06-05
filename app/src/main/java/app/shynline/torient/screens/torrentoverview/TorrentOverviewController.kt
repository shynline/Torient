package app.shynline.torient.screens.torrentoverview

import app.shynline.torient.domain.helper.timer.TimerController
import app.shynline.torient.model.defaultTorrentOverView
import app.shynline.torient.screens.common.BaseController
import app.shynline.torient.torrent.mediator.usecases.GetTorrentSchemeUseCase
import app.shynline.torient.torrent.torrent.Torrent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.*

class TorrentOverviewController(
    coroutineDispatcher: CoroutineDispatcher,
    private val timerController: TimerController,
    private val getTorrentSchemeUseCase: GetTorrentSchemeUseCase,
    private val torrent: Torrent
) : BaseController(coroutineDispatcher), TorrentOverviewViewMvc.Listener {

    private lateinit var viewMvc: TorrentOverviewViewMvc
    private lateinit var infoHash: String

    fun bind(viewMvc: TorrentOverviewViewMvc) {
        this.viewMvc = viewMvc
    }

    override fun loadState(state: HashMap<String, Any>?) {
    }

    override fun saveState(): HashMap<String, Any>? {
        return null
    }

    override fun onStart() {
        viewMvc.registerListener(this)
        timerController.schedule(this, 100, 1000) {
            periodicTask()
        }
    }

    override fun onStop() {
        viewMvc.unRegisterListener(this)
        timerController.cancel(this)
    }

    fun setTorrent(infoHash: String) {
        this.infoHash = infoHash
        controllerScope.launch {
            update()
        }
    }

    private suspend fun update() {
        // If it returns null this torrent doesn't have meta data (added by magnet)
        var torrentOverview = torrent.getTorrentOverview(infoHash)
        val torrentSchema =
            getTorrentSchemeUseCase(GetTorrentSchemeUseCase.In(infoHash)).torrentScheme ?: return
        if (torrentOverview != null) {
            torrentOverview.progress = if (torrentSchema.isFinished) {
                1f
            } else {
                torrentSchema.progress
            }
            torrentOverview.lastSeenComplete = torrentSchema.lastSeenComplete
            torrentOverview.name = torrentSchema.name

        } else {
            torrentOverview = defaultTorrentOverView(infoHash, torrentSchema)
        }
        torrentOverview?.let { viewMvc.updateUi(it) }
    }

    private fun periodicTask() = controllerScope.launch {
        update()
    }


}