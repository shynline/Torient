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
import kotlinx.coroutines.launch

class NewTorrentController(
    private val torrentMediator: TorrentMediator,
    private val torrentDataSource: TorrentDataSource,
    private val torrentFilePriorityDataSource: TorrentFilePriorityDataSource
) : BaseController(), NewTorrentViewMvc.Listener {
    private var viewMvc: NewTorrentViewMvc? = null
    private var pageNavigationHelper: PageNavigationHelper? = null
    private var fragmentRequestHelper: FragmentRequestHelper? = null
    private var currentTorrent: TorrentModel? = null


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
        currentTorrent = torrentMediator.getTorrentModel(infoHash = infoHash)
        if (currentTorrent != null) {
            viewMvc!!.showTorrent(currentTorrent!!)
        } else {
            close()
        }
    }

    override fun onStart() {
        viewMvc!!.registerListener(this)
    }

    override fun onStop() {
        viewMvc!!.unRegisterListener(this)
    }

    override fun downloadTorrent() {
        controllerScope.launch {
            currentTorrent?.let {
                if (torrentDataSource.getTorrent(it.infoHash) == null) {
                    torrentDataSource.insertTorrent(
                        TorrentSchema(
                            infoHash = it.infoHash,
                            name = it.name,
                            magnet = it.magnet,
                            userState = TorrentUserState.ACTIVE
                        )
                    )
                    initiateFilePriority(it.infoHash, it.numFiles)
                }
            }
            close()
        }
    }

    override fun addTorrent() {
        controllerScope.launch {
            currentTorrent?.let {
                if (torrentDataSource.getTorrent(it.infoHash) == null) {
                    torrentDataSource.insertTorrent(
                        TorrentSchema(
                            infoHash = it.infoHash,
                            magnet = it.magnet,
                            name = it.name,
                            userState = TorrentUserState.PAUSED
                        )
                    )
                    initiateFilePriority(it.infoHash, it.numFiles)
                }
            }
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
        pageNavigationHelper!!.back()
    }

    override fun loadState(state: HashMap<String, Any>?) {
    }

    override fun saveState(): HashMap<String, Any>? {
        return null
    }

    override fun unbind() {
        viewMvc = null
        pageNavigationHelper = null
        fragmentRequestHelper = null
    }

}