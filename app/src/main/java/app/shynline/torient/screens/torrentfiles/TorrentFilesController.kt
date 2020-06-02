package app.shynline.torient.screens.torrentfiles

import app.shynline.torient.database.entities.TorrentFilePrioritySchema
import app.shynline.torient.domain.helper.timer.TimerController
import app.shynline.torient.model.FilePriority
import app.shynline.torient.model.TorrentModel
import app.shynline.torient.screens.common.BaseController
import app.shynline.torient.screens.common.requesthelper.FragmentRequestHelper
import app.shynline.torient.torrent.mediator.usecases.GetTorrentFilePriorityUseCase
import app.shynline.torient.torrent.mediator.usecases.GetTorrentModelUseCase
import app.shynline.torient.torrent.mediator.usecases.GetTorrentSchemeUseCase
import app.shynline.torient.torrent.mediator.usecases.UpdateTorrentFilePriorityUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch

class TorrentFilesController(
    coroutineDispatcher: CoroutineDispatcher,
    private val getTorrentSchemeUseCase: GetTorrentSchemeUseCase,
    private val updateTorrentFilePriorityUseCase: UpdateTorrentFilePriorityUseCase,
    private val getTorrentFilePriorityUseCase: GetTorrentFilePriorityUseCase,
    private val getTorrentModelUseCase: GetTorrentModelUseCase,
    private val timerController: TimerController
) : BaseController(coroutineDispatcher), TorrentFilesViewMvc.Listener {

    companion object {
        private const val INFO_HASH = "infohash"
        private const val VIEW_STATE = "viewstate"
    }

    private lateinit var viewMvc: TorrentFilesViewMvc
    private lateinit var infoHash: String
    private var lastProgressHashCode = 0
    private var lastProgress: List<Long>? = null
    private var torrentModel: TorrentModel? = null
    private lateinit var torrentPriority: TorrentFilePrioritySchema
    private var savedState: HashMap<String, Any>? = null
    private lateinit var fragmentRequestHelper: FragmentRequestHelper
    private var isFilesLoaded = false

    fun bind(viewMvc: TorrentFilesViewMvc, fragmentRequestHelper: FragmentRequestHelper) {
        this.viewMvc = viewMvc
        this.fragmentRequestHelper = fragmentRequestHelper
    }

    override fun loadState(state: HashMap<String, Any>?) {
        state?.let {
            if (it[INFO_HASH] != infoHash)
                return
            savedState = it
        }
    }

    private fun applyViewState() {
        @Suppress("UNCHECKED_CAST")
        viewMvc.loadState(savedState?.get(VIEW_STATE) as? HashMap<String, Any>)
        savedState = null
    }

    override fun saveState(): HashMap<String, Any>? {
        return HashMap<String, Any>().apply {
            put(INFO_HASH, infoHash)
            put(VIEW_STATE, viewMvc.saveState())
        }
    }


    override fun onStart() {
        viewMvc.registerListener(this)
        timerController.schedule(this, 1000, 1000) {
            periodicTask()
        }
    }

    override fun onStop() {
        viewMvc.unRegisterListener(this)
        timerController.cancel(this)
    }

    fun setTorrent(infoHash: String) {
        this.infoHash = infoHash
        loadTorrentFiles()
    }


    private fun loadTorrentFiles() = controllerScope.launch {
        torrentModel =
            getTorrentModelUseCase(GetTorrentModelUseCase.In(infoHash = infoHash)).torrentModel
        torrentPriority =
            getTorrentFilePriorityUseCase(GetTorrentFilePriorityUseCase.In(infoHash)).torrentPriority

        if (torrentModel == null || torrentPriority.filePriority == null) {
            // Meta data is not available
            // And its loading if user state is Active
            return@launch
        }

        isFilesLoaded = true
        viewMvc.showTorrent(torrentModel!!)

        updateFileProgress()
        updateFilePriorityUi()
        applyViewState()
    }

    private fun periodicTask() = controllerScope.launch {
        if (isFilesLoaded) {
            updateFileProgress()
        } else {
            loadTorrentFiles()
        }
    }


    private fun updateFilePriorityUi() {
        viewMvc.updateFilePriority(torrentPriority.filePriority!!)
    }

    override fun onDownloadCheckBoxClicked(index: Int, download: Boolean) {
        controllerScope.launch {
            if (torrentPriority.filePriority!![index].active != download) {
                torrentPriority.filePriority!![index].active = download
                updateTorrentFilePriority(index)
            }
        }
    }

    override fun onPriorityClicked(index: Int) {
        controllerScope.launch {
            when (torrentPriority.filePriority!![index].priority) {
                FilePriority.NORMAL -> {
                    torrentPriority.filePriority!![index].priority = FilePriority.HIGH
                }
                FilePriority.HIGH -> {
                    torrentPriority.filePriority!![index].priority = FilePriority.LOW
                }
                FilePriority.LOW -> {
                    torrentPriority.filePriority!![index].priority = FilePriority.NORMAL
                }
                FilePriority.MIXED -> {
                    throw IllegalStateException("Files can not have mixed priority.")
                }
            }
            updateTorrentFilePriority(index)
            updateFilePriorityUi()
        }
    }

    private suspend fun updateTorrentFilePriority(index: Int) {
        updateTorrentFilePriorityUseCase(
            UpdateTorrentFilePriorityUseCase.In(
                infoHash, index, torrentPriority
            )
        )
    }

    override fun isFileCompleted(index: Int): Boolean {
        if (torrentModel?.filesSize == null || lastProgress == null) {
            return false
        }
        return torrentModel!!.filesSize!![index] == lastProgress!![index]
    }

    override fun saveFile(index: Int) {
        if (torrentModel == null)
            return
        fragmentRequestHelper.saveToDownload(
            torrentModel!!.filesPath!![index],
            torrentModel!!.infoHash
        )
    }

    private suspend fun updateFileProgress() {
        getTorrentSchemeUseCase(GetTorrentSchemeUseCase.In(infoHash)).torrentScheme!!.fileProgress?.let { fileProgress ->
            val hash = fileProgress.hashCode()
            if (hash != lastProgressHashCode) {
                lastProgress = fileProgress
                lastProgressHashCode = hash
                viewMvc.updateFileProgress(fileProgress)
            }
        }
    }
}