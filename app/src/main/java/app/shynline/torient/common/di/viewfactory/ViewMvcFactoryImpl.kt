package app.shynline.torient.common.di.viewfactory

import android.view.LayoutInflater
import android.view.ViewGroup
import app.shynline.torient.screens.newtorrent.NewTorrentViewMvc
import app.shynline.torient.screens.newtorrent.NewTorrentViewMvcImpl
import app.shynline.torient.screens.torrentslist.TorrentListViewMvc
import app.shynline.torient.screens.torrentslist.TorrentListViewMvcImpl

class ViewMvcFactoryImpl :
    ViewMvcFactory {

    override fun getTorrentListViewMvc(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): TorrentListViewMvc {
        return TorrentListViewMvcImpl(inflater, parent)
    }

    override fun getNewTorrentViewMvc(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): NewTorrentViewMvc {
        return NewTorrentViewMvcImpl(inflater, parent)
    }
}