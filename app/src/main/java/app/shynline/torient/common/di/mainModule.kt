package app.shynline.torient.common.di

import app.shynline.torient.common.di.viewfactory.ViewMvcFactory
import app.shynline.torient.common.di.viewfactory.ViewMvcFactoryImpl
import app.shynline.torient.screens.newtorrent.NewTorrentController
import app.shynline.torient.screens.newtorrent.NewTorrentFragment
import app.shynline.torient.screens.torrentslist.TorrentsListController
import app.shynline.torient.screens.torrentslist.TorrentsListFragment
import app.shynline.torient.torrent.torrent.SubscriptionMediator
import app.shynline.torient.torrent.torrent.Torrent
import app.shynline.torient.torrent.torrent.TorrentImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val mainModule = module {
    single<ViewMvcFactory> {
        ViewMvcFactoryImpl()
    }
    scope<TorrentsListFragment> {
        scoped {
            TorrentsListController(get(), get(), get(), get())
        }
    }
    scope<NewTorrentFragment> {
        scoped {
            NewTorrentController(get(), get())
        }
    }
    single<Torrent> {
        TorrentImpl(androidContext())
    }
    single {
        SubscriptionMediator(get())
    }
}