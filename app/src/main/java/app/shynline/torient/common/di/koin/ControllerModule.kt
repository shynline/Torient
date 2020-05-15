package app.shynline.torient.common.di.koin

import app.shynline.torient.screens.newmagnet.NewMagnetController
import app.shynline.torient.screens.newmagnet.NewMagnetFragment
import app.shynline.torient.screens.newtorrent.NewTorrentController
import app.shynline.torient.screens.newtorrent.NewTorrentFragment
import app.shynline.torient.screens.torrentfiles.TorrentFilesController
import app.shynline.torient.screens.torrentfiles.TorrentFilesFragment
import app.shynline.torient.screens.torrentoverview.TorrentOverviewController
import app.shynline.torient.screens.torrentoverview.TorrentOverviewFragment
import app.shynline.torient.screens.torrentpreference.TorrentPreferenceController
import app.shynline.torient.screens.torrentpreference.TorrentPreferenceFragment
import app.shynline.torient.screens.torrentslist.TorrentsListController
import app.shynline.torient.screens.torrentslist.TorrentsListFragment
import org.koin.dsl.module

val controllerModule = module {
    scope<TorrentsListFragment> {
        scoped {
            TorrentsListController(get(), get(), get())
        }
    }
    scope<NewTorrentFragment> {
        scoped {
            NewTorrentController(get(), get())
        }
    }
    scope<NewMagnetFragment> {
        scoped {
            NewMagnetController(get(), get())
        }
    }
    scope<TorrentOverviewFragment> {
        scoped {
            TorrentOverviewController()
        }
    }
    scope<TorrentFilesFragment> {
        scoped {
            TorrentFilesController()
        }
    }
    scope<TorrentPreferenceFragment> {
        scoped {
            TorrentPreferenceController()
        }
    }
}