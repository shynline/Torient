package app.shynline.torient.screens.newtorrent

import app.shynline.torient.database.TorrentUserState
import app.shynline.torient.database.datasource.TorrentDataSource
import app.shynline.torient.database.entities.TorrentSchema
import app.shynline.torient.model.TorrentDetail
import app.shynline.torient.screens.common.BaseController
import app.shynline.torient.screens.common.navigationhelper.PageNavigationHelper
import app.shynline.torient.screens.common.requesthelper.FragmentRequestHelper
import app.shynline.torient.usecases.GetTorrentDetailUseCase
import kotlinx.coroutines.launch

class NewTorrentController(
    private val getTorrentDetailUseCase: GetTorrentDetailUseCase,
    private val torrentDataSource: TorrentDataSource
) : BaseController(), NewTorrentViewMvc.Listener {
    private var viewMvc: NewTorrentViewMvc? = null
    private var pageNavigationHelper: PageNavigationHelper? = null
    private var fragmentRequestHelper: FragmentRequestHelper? = null
    private var currentTorrent: TorrentDetail? = null


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
        currentTorrent = getTorrentDetailUseCase.execute(infoHash = infoHash)
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
                torrentDataSource.insertTorrent(
                    TorrentSchema(
                        it.infoHash,
                        it.magnet,
                        TorrentUserState.ACTIVE
                    )
                )
            }
            close()
        }
    }

    override fun addTorrent() {
        controllerScope.launch {
            currentTorrent?.let {
                torrentDataSource.insertTorrent(
                    TorrentSchema(
                        it.infoHash,
                        it.magnet,
                        TorrentUserState.PAUSED
                    )
                )
            }
            close()
        }
    }

    private fun close() {
        pageNavigationHelper!!.back()
    }

    fun unbind() {
        viewMvc = null
        pageNavigationHelper = null
        fragmentRequestHelper = null
    }

}