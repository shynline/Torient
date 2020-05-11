package app.shynline.torient.screens.newmagnet

import app.shynline.torient.database.datasource.TorrentDataSource
import app.shynline.torient.database.entities.TorrentSchema
import app.shynline.torient.database.states.TorrentUserState
import app.shynline.torient.model.TorrentIdentifier
import app.shynline.torient.screens.common.BaseController
import app.shynline.torient.screens.common.navigationhelper.PageNavigationHelper
import app.shynline.torient.torrent.mediator.TorrentMediator
import app.shynline.torient.torrent.utils.Magnet
import kotlinx.coroutines.launch

class NewMagnetController(
    private val torrentMediator: TorrentMediator,
    private val torrentDataSource: TorrentDataSource
) : BaseController(), NewMagnetViewMvc.Listener {

    private var viewMvc: NewMagnetViewMvc? = null
    private var pageNavigationHelper: PageNavigationHelper? = null
    private var currentMagnet: Magnet? = null
    private lateinit var magnet: String

    fun bind(
        viewMvc: NewMagnetViewMvc,
        pageNavigationHelper: PageNavigationHelper
    ) {
        this.viewMvc = viewMvc
        this.pageNavigationHelper = pageNavigationHelper
    }


    override fun onStart() {
        viewMvc!!.registerListener(this)
    }

    override fun onStop() {
        viewMvc!!.unRegisterListener(this)
    }

    fun unbind() {
        viewMvc = null
        pageNavigationHelper = null
    }

    fun showTorrent(magnet: String) = controllerScope.launch {
        this@NewMagnetController.magnet = magnet
        currentMagnet = Magnet.parse(magnet)
        if (currentMagnet == null) {
            close()
            return@launch
        }
        viewMvc!!.showMagnet(currentMagnet!!)
        // Attempting to get the torrent metaData
        val torrentDetail =
            torrentMediator.getTorrentModel(
                identifier = TorrentIdentifier(
                    currentMagnet!!.infoHash!!,
                    magnet
                )
            )

        torrentDetail?.let {
            // If we found the metadata we navigate to NewTorrentFragment
            close()
            pageNavigationHelper!!.showNewTorrentDialog(it.infoHash)
        }
    }

    override fun onDownloadClicked() {
        controllerScope.launch {
            torrentDataSource.insertTorrent(
                TorrentSchema(
                    infoHash = currentMagnet!!.infoHash!!,
                    magnet = magnet,
                    userState = TorrentUserState.ACTIVE,
                    name = currentMagnet!!.name!!
                )
            )
            close()
        }
    }

    private fun close() {
        pageNavigationHelper!!.back()
    }
}