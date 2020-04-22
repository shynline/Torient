package app.shynline.torient.common.di.viewfactory

import android.view.LayoutInflater
import android.view.ViewGroup
import app.shynline.torient.screens.torrentslist.TorrentListViewMvc

interface ViewMvcFactory {
    fun getTorrentListViewMvc(inflater: LayoutInflater, parent: ViewGroup?): TorrentListViewMvc
}