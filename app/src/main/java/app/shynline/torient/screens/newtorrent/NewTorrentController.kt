package app.shynline.torient.screens.newtorrent

import app.shynline.torient.database.common.states.TorrentUserState
import app.shynline.torient.model.TorrentModel
import app.shynline.torient.screens.common.BaseController
import app.shynline.torient.screens.common.navigationhelper.PageNavigationHelper
import app.shynline.torient.screens.common.requesthelper.FragmentRequestHelper
import app.shynline.torient.torrent.mediator.usecases.AddTorrentToDataBaseUseCase
import app.shynline.torient.torrent.mediator.usecases.GetTorrentModelUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch

class NewTorrentController(
    coroutineDispatcher: CoroutineDispatcher,
    val getTorrentModelUseCase: GetTorrentModelUseCase,
    val addTorrentToDataBaseUseCase: AddTorrentToDataBaseUseCase
) : BaseController(coroutineDispatcher), NewTorrentViewMvc.Listener {

    private var viewMvc: NewTorrentViewMvc? = null
    private var pageNavigationHelper: PageNavigationHelper? = null
    private var fragmentRequestHelper: FragmentRequestHelper? = null
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

    override fun cleanUp() {
        viewMvc = null
        pageNavigationHelper = null
        fragmentRequestHelper = null
    }

    fun showTorrent(infoHash: String) = controllerScope.launch {
        // Initiate currentTorrent and Update the UI if torrent model ( meta data ) exists
        getTorrentModelUseCase(GetTorrentModelUseCase.In(infoHash = infoHash)).torrentModel?.let {
            currentTorrent = it
            viewMvc!!.showTorrent(currentTorrent)
            return@launch
        }
        // Close this screen if meta data is not available
        close()
    }

    override fun onStart() {
        viewMvc!!.registerListener(this)
    }

    override fun onStop() {
        viewMvc!!.unRegisterListener(this)
    }

    override fun downloadTorrent() {
        controllerScope.launch {
            addTorrentToDataBaseUseCase(
                AddTorrentToDataBaseUseCase.In(
                    currentTorrent.infoHash,
                    currentTorrent.name,
                    currentTorrent.magnet,
                    TorrentUserState.ACTIVE,
                    true,
                    currentTorrent.numFiles
                )
            )
            close()
        }
    }

    override fun addTorrent() {
        controllerScope.launch {
            addTorrentToDataBaseUseCase(
                AddTorrentToDataBaseUseCase.In(
                    currentTorrent.infoHash,
                    currentTorrent.name,
                    currentTorrent.magnet,
                    TorrentUserState.PAUSED,
                    true,
                    currentTorrent.numFiles
                )
            )
            close()
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

}