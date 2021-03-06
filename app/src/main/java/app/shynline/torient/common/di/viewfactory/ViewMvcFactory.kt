package app.shynline.torient.common.di.viewfactory

import android.view.LayoutInflater
import android.view.ViewGroup
import app.shynline.torient.screens.addmagnet.AddMagnetViewMvc
import app.shynline.torient.screens.newmagnet.NewMagnetViewMvc
import app.shynline.torient.screens.newtorrent.NewTorrentViewMvc
import app.shynline.torient.screens.preference.PreferenceViewMvc
import app.shynline.torient.screens.torrentfiles.TorrentFilesViewMvc
import app.shynline.torient.screens.torrentoverview.TorrentOverviewViewMvc
import app.shynline.torient.screens.torrentpreference.TorrentPreferenceViewMvc
import app.shynline.torient.screens.torrentslist.TorrentListViewMvc

interface ViewMvcFactory {
    fun getTorrentListViewMvc(inflater: LayoutInflater, parent: ViewGroup?): TorrentListViewMvc
    fun getNewTorrentViewMvc(inflater: LayoutInflater, parent: ViewGroup?): NewTorrentViewMvc
    fun getNewMagnetViewMvc(inflater: LayoutInflater, parent: ViewGroup?): NewMagnetViewMvc
    fun getAddMagnetViewMvc(inflater: LayoutInflater, parent: ViewGroup?): AddMagnetViewMvc
    fun getTorrentOverViewViewMvc(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): TorrentOverviewViewMvc
    fun getTorrentFilesViewMvc(inflater: LayoutInflater, parent: ViewGroup?): TorrentFilesViewMvc
    fun getTorrentPreferenceViewMvc(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): TorrentPreferenceViewMvc

    fun getPreferenceViewMvc(inflater: LayoutInflater, parent: ViewGroup?): PreferenceViewMvc
}