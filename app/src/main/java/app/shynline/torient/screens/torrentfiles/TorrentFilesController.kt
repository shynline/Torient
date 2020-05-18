package app.shynline.torient.screens.torrentfiles

import app.shynline.torient.database.datasource.torrent.TorrentDataSource
import app.shynline.torient.database.datasource.torrentfilepriority.TorrentFilePriorityDataSource
import app.shynline.torient.database.entities.TorrentFilePrioritySchema
import app.shynline.torient.database.entities.TorrentSchema
import app.shynline.torient.model.TorrentFilePriority
import app.shynline.torient.screens.common.BaseController
import app.shynline.torient.torrent.mediator.TorrentMediator
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.fixedRateTimer

class TorrentFilesController(
    private val torrentMediator: TorrentMediator,
    private val torrentDataSource: TorrentDataSource,
    private val torrentFilePriorityDataSource: TorrentFilePriorityDataSource
) : BaseController(), TorrentFilesViewMvc.Listener {

    private var viewMvc: TorrentFilesViewMvc? = null
    private lateinit var infoHash: String
    private var periodicTimer: Timer? = null
    private var lastProgressHashCode = 0

    fun bind(viewMvc: TorrentFilesViewMvc) {
        this.viewMvc = viewMvc
    }

    fun unbind() {
        viewMvc = null
    }

    override fun onStart() {
        viewMvc!!.registerListener(this)
        periodicTimer = fixedRateTimer(
            name = "periodicTaskTorrentFiles",
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
        loadTorrentFiles()
    }

    private lateinit var torrentPriority: TorrentFilePrioritySchema

    private fun loadTorrentFiles() = controllerScope.launch {
        val torrentModel = torrentMediator.getTorrentModel(infoHash = infoHash)
        val torrentSchema = torrentDataSource.getTorrent(infoHash)!! // It's not null
        torrentPriority = torrentFilePriorityDataSource.getPriority(infoHash)
        if (torrentModel == null) {
            // Meta data is not available
            // And its loading if user state is Active
            return@launch
        }
        if (torrentPriority.filePriority == null) {
            torrentPriority.filePriority =
                MutableList(torrentModel.numFiles) { TorrentFilePriority.default() }
        }
        viewMvc!!.showTorrent(torrentModel)

        updateFileProgress(torrentSchema)
        updateFilePriority()
    }

    private fun periodicTask() = controllerScope.launch {
        val torrentSchema = torrentDataSource.getTorrent(infoHash)!! // It's not null
        updateFileProgress(torrentSchema)
    }


    private fun updateFilePriority() {
        viewMvc!!.updateFilePriority(torrentPriority.filePriority!!)
    }

    private fun updateFileProgress(torrentSchema: TorrentSchema) {
        torrentSchema.fileProgress?.let { fileProgress ->
            val hash = fileProgress.hashCode()
            if (hash != lastProgressHashCode) {
                viewMvc!!.updateFileProgress(fileProgress)
            }
            lastProgressHashCode = hash
        }
    }
}