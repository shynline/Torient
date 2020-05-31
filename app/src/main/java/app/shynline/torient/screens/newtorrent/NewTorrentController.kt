package app.shynline.torient.screens.newtorrent

import app.shynline.torient.database.common.states.TorrentUserState
import app.shynline.torient.database.datasource.torrent.TorrentDataSource
import app.shynline.torient.database.datasource.torrentfilepriority.TorrentFilePriorityDataSource
import app.shynline.torient.database.entities.TorrentSchema
import app.shynline.torient.model.TorrentFilePriority
import app.shynline.torient.model.TorrentModel
import app.shynline.torient.screens.common.BaseController
import app.shynline.torient.screens.common.navigationhelper.PageNavigationHelper
import app.shynline.torient.screens.common.requesthelper.FragmentRequestHelper
import app.shynline.torient.torrent.mediator.TorrentMediator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch

class NewTorrentController(
    coroutineDispatcher: CoroutineDispatcher,
    private val torrentMediator: TorrentMediator,
    private val torrentDataSource: TorrentDataSource,
    private val torrentFilePriorityDataSource: TorrentFilePriorityDataSource
) : BaseController(coroutineDispatcher), NewTorrentViewMvc.Listener {

    private lateinit var viewMvc: NewTorrentViewMvc
    private lateinit var pageNavigationHelper: PageNavigationHelper
    private lateinit var fragmentRequestHelper: FragmentRequestHelper
    private lateinit var currentTorrent: TorrentModel


    fun bind(
        viewMvc: NewTorrentViewMvc,
        pageNavigationHelper: PageNavigationHelper,
        fragmentRequestHelper: FragmentRequestHelper
    ) {
        this.viewMvc = viewMvc
        this.pageNavigationHelper = pageNavigationHelper
        this.fragmentRequestHelper = fragmentRequestHelper
    }

    fun showTorrent(infoHash: String) = controllerScope.launch {
        // Initiate currentTorrent and Update the UI if torrent model ( meta data ) exists
        torrentMediator.getTorrentModel(infoHash = infoHash)?.let {
            currentTorrent = it
            viewMvc.showTorrent(currentTorrent)
            return@launch
        }
        // Close this screen if meta data is not available
        close()
    }

    override fun onStart() {
        viewMvc.registerListener(this)
    }

    override fun onStop() {
        viewMvc.unRegisterListener(this)
    }

    override fun downloadTorrent() {
        controllerScope.launch {
            createAndInitiateIfDoesNotExists(TorrentUserState.ACTIVE)
            close()
        }
    }

    private suspend fun createAndInitiateIfDoesNotExists(state: TorrentUserState) {
        if (torrentDataSource.getTorrent(currentTorrent.infoHash) == null) {
            torrentDataSource.insertTorrent(
                TorrentSchema(
                    infoHash = currentTorrent.infoHash,
                    name = currentTorrent.name,
                    magnet = currentTorrent.magnet,
                    userState = state
                )
            )
            initiateFilePriority(currentTorrent.infoHash, currentTorrent.numFiles)
        }
    }

    override fun addTorrent() {
        controllerScope.launch {
            createAndInitiateIfDoesNotExists(TorrentUserState.PAUSED)
            close()
        }
    }

    private suspend fun initiateFilePriority(infoHash: String, numFile: Int) {
        val p = torrentFilePriorityDataSource.getPriority(infoHash)
        if (p.filePriority == null) {
            // Generate default priorities
            p.filePriority = MutableList(numFile) { TorrentFilePriority.default() }
            // Update database with generated priorities
            torrentFilePriorityDataSource.setPriority(p)
        }
    }

    private fun close() {
        pageNavigationHelper.back()
    }

    override fun loadState(state: HashMap<String, Any>?) {
    }

    override fun saveState(): HashMap<String, Any>? {
        return null
    }

}