package app.shynline.torient.common.di.koin

import app.shynline.torient.screens.newmagnet.NewMagnetController
import app.shynline.torient.screens.newmagnet.NewMagnetFragment
import app.shynline.torient.screens.newtorrent.NewTorrentController
import app.shynline.torient.screens.newtorrent.NewTorrentFragment
import app.shynline.torient.screens.torrentdetail.TorrentDetailController
import app.shynline.torient.screens.torrentdetail.TorrentDetailFragment
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
    scope<TorrentDetailFragment> {
        scoped {
            TorrentDetailController(get(), get(), get())
        }
    }
}