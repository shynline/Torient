package app.shynline.torient.screens.newmagnet

import app.shynline.torient.domain.database.common.states.TorrentUserState
import app.shynline.torient.domain.models.TorrentIdentifier
import app.shynline.torient.screens.common.BaseController
import app.shynline.torient.screens.common.navigationhelper.PageNavigationHelper
import app.shynline.torient.domain.mediator.usecases.AddTorrentToDataBaseUseCase
import app.shynline.torient.domain.mediator.usecases.GetTorrentModelUseCase
import app.shynline.torient.domain.torrentmanager.utils.Magnet
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch

class NewMagnetController(
    coroutineDispatcher: CoroutineDispatcher,
    private val getTorrentModelUseCase: GetTorrentModelUseCase,
    private val addTorrentToDataBaseUseCase: AddTorrentToDataBaseUseCase
) : BaseController(coroutineDispatcher), NewMagnetViewMvc.Listener {

    private var viewMvc: NewMagnetViewMvc? = null
    private var pageNavigationHelper: PageNavigationHelper? = null
    private lateinit var currentMagnet: Magnet
    private lateinit var magnet: String

    fun bind(
        viewMvc: NewMagnetViewMvc,
        pageNavigationHelper: PageNavigationHelper
    ) {
        this.viewMvc = viewMvc
        this.pageNavigationHelper = pageNavigationHelper
    }

    override fun cleanUp() {
        viewMvc = null
        pageNavigationHelper = null
    }

    override fun onStart() {
        viewMvc!!.registerListener(this)
    }

    override fun onStop() {
        viewMvc!!.unRegisterListener(this)
    }

    override fun loadState(state: HashMap<String, Any>?) {
    }

    override fun saveState(): HashMap<String, Any>? {
        return null
    }

    fun showTorrent(magnet: String) = controllerScope.launch {
        this@NewMagnetController.magnet = magnet
        Magnet.parse(magnet)?.let {
            currentMagnet = it

            viewMvc!!.showMagnet(currentMagnet)
            // Attempting to get the torrent metaData
            getTorrentModelUseCase(
                GetTorrentModelUseCase.In(
                    identifier = TorrentIdentifier(
                        currentMagnet.infoHash!!,
                        magnet
                    )
                )
            ).torrentModel?.let { torrentModel ->
                // If we found the metadata we navigate to NewTorrentFragment
                close()
                pageNavigationHelper!!.showNewTorrentDialog(torrentModel.infoHash)
            }
            return@launch
        }
        close()
    }

    override fun onDownloadClicked() {
        controllerScope.launch {
            addTorrentToDataBaseUseCase(
                AddTorrentToDataBaseUseCase.In(
                    currentMagnet.infoHash!!,
                    currentMagnet.name ?: "",
                    magnet,
                    TorrentUserState.ACTIVE
                )
            )
            close()
        }
    }

    private fun close() {
        pageNavigationHelper!!.back()
    }

}