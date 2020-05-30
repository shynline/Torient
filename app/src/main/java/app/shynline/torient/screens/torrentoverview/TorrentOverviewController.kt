package app.shynline.torient.screens.torrentoverview

import app.shynline.torient.database.datasource.torrent.TorrentDataSource
import app.shynline.torient.model.TorrentOverview
import app.shynline.torient.screens.common.BaseController
import app.shynline.torient.torrent.mediator.TorrentMediator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.fixedRateTimer

class TorrentOverviewController(
    coroutineDispatcher: CoroutineDispatcher,
    private val torrentMediator: TorrentMediator,
    private val torrentDataSource: TorrentDataSource
) : BaseController(coroutineDispatcher), TorrentOverviewViewMvc.Listener {

    private var viewMvc: TorrentOverviewViewMvc? = null
    private lateinit var infoHash: String
    private var periodicTimer: Timer? = null

    fun bind(viewMvc: TorrentOverviewViewMvc) {
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
        var torrentOverview = torrentMediator.torrentOverview(infoHash)
        val torrentSchema = torrentDataSource.getTorrent(infoHash)
        if (torrentOverview != null) {
            if (torrentSchema != null) {
                torrentOverview.progress = if (torrentSchema.isFinished) {
                    1f
                } else {
                    torrentSchema.progress
                }
                torrentOverview.lastSeenComplete = torrentSchema.lastSeenComplete
                torrentOverview.name = torrentSchema.name
            }
        } else {
            if (torrentSchema != null) {
                torrentOverview = TorrentOverview(
                    name = torrentSchema.name,
                    infoHash = infoHash,
                    progress = 0f,
                    numPiece = 0,
                    pieceLength = 0,
                    size = 0,
                    userState = torrentSchema.userState,
                    creator = "",
                    comment = "",
                    createdDate = 0,
                    private = false,
                    lastSeenComplete = torrentSchema.lastSeenComplete
                )
            }
        }
        torrentOverview?.let { viewMvc!!.updateUi(it) }
    }

    private fun periodicTask() = controllerScope.launch {
        update()
    }


}