package app.shynline.torient.screens.torrentslist

import app.shynline.torient.screens.common.navigationhelper.PageNavigationHelper
import app.shynline.torient.screens.common.requesthelper.FragmentRequestHelper
import app.shynline.torient.usecases.GetTorrentDetailUseCase
import app.shynline.torient.usecases.GetTorrentIdentifierUseCase

class TorrentsListController(
    private val getTorrentIdentifierUseCase: GetTorrentIdentifierUseCase,
    private val getTorrentDetailUseCase: GetTorrentDetailUseCase
) : TorrentListViewMvc.Listener {

    private var viewMvc: TorrentListViewMvc? = null
    private var fragmentRequestHelper: FragmentRequestHelper? = null
    private var pageNavigationHelper: PageNavigationHelper? = null


    fun bind(
        viewMvc: TorrentListViewMvc, fragmentRequestHelper: FragmentRequestHelper,
        pageNavigationHelper: PageNavigationHelper
    ) {
        this.viewMvc = viewMvc
        this.fragmentRequestHelper = fragmentRequestHelper
        this.pageNavigationHelper = pageNavigationHelper
    }

    fun unbind() {
        viewMvc = null
        fragmentRequestHelper = null
        pageNavigationHelper = null
    }

    fun onStart() {
        viewMvc!!.registerListener(this)
    }

    fun onStop() {
        viewMvc!!.unRegisterListener(this)
    }

    override fun addTorrentFile() {
        fragmentRequestHelper!!.openTorrentFile(REQUEST_ID_OPEN_TORRENT_FILE)
    }

    fun openTorrentFile(torrentData: ByteArray) {
        val identifier = getTorrentIdentifierUseCase.execute(torrentData)
        pageNavigationHelper!!.showNewTorrentDialog(identifier.infoHash)
    }




}