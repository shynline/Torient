package app.shynline.torient.common.di.viewfactory

import android.view.LayoutInflater
import android.view.ViewGroup
import app.shynline.torient.screens.addmagnet.AddMagnetViewMvc
import app.shynline.torient.screens.addmagnet.AddMagnetViewMvcImpl
import app.shynline.torient.screens.newmagnet.NewMagnetViewMvc
import app.shynline.torient.screens.newmagnet.NewMagnetViewMvcImpl
import app.shynline.torient.screens.newtorrent.NewTorrentViewMvc
import app.shynline.torient.screens.newtorrent.NewTorrentViewMvcImpl
import app.shynline.torient.screens.preference.PreferenceViewMvc
import app.shynline.torient.screens.preference.PreferenceViewMvcImpl
import app.shynline.torient.screens.torrentfiles.TorrentFilesViewMvc
import app.shynline.torient.screens.torrentfiles.TorrentFilesViewMvcImpl
import app.shynline.torient.screens.torrentoverview.TorrentOverviewViewMvc
import app.shynline.torient.screens.torrentoverview.TorrentOverviewViewMvcImpl
import app.shynline.torient.screens.torrentpreference.TorrentPreferenceViewMvc
import app.shynline.torient.screens.torrentpreference.TorrentPreferenceViewMvcImpl
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

    override fun getNewMagnetViewMvc(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): NewMagnetViewMvc {
        return NewMagnetViewMvcImpl(inflater, parent)
    }

    override fun getAddMagnetViewMvc(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): AddMagnetViewMvc {
        return AddMagnetViewMvcImpl(inflater, parent)
    }


    override fun getTorrentFilesViewMvc(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): TorrentFilesViewMvc {
        return TorrentFilesViewMvcImpl(inflater, parent)
    }

    override fun getTorrentOverViewViewMvc(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): TorrentOverviewViewMvc {
        return TorrentOverviewViewMvcImpl(inflater, parent)
    }

    override fun getTorrentPreferenceViewMvc(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): TorrentPreferenceViewMvc {
        return TorrentPreferenceViewMvcImpl(inflater, parent)
    }

    override fun getPreferenceViewMvc(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): PreferenceViewMvc {
        return PreferenceViewMvcImpl(inflater, parent)
    }
}