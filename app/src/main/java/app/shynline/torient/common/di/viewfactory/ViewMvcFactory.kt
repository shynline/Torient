package app.shynline.torient.common.di.viewfactory

import android.view.LayoutInflater
import android.view.ViewGroup
import app.shynline.torient.screens.addmagnet.AddMagnetViewMvc
import app.shynline.torient.screens.newmagnet.NewMagnetViewMvc
import app.shynline.torient.screens.newtorrent.NewTorrentViewMvc
import app.shynline.torient.screens.torrentslist.TorrentListViewMvc

interface ViewMvcFactory {
    fun getTorrentListViewMvc(inflater: LayoutInflater, parent: ViewGroup?): TorrentListViewMvc
    fun getNewTorrentViewMvc(inflater: LayoutInflater, parent: ViewGroup?): NewTorrentViewMvc
    fun getNewMagnetViewMvc(inflater: LayoutInflater, parent: ViewGroup?): NewMagnetViewMvc
    fun getAddMagnetViewMvc(inflater: LayoutInflater, parent: ViewGroup?): AddMagnetViewMvc
}