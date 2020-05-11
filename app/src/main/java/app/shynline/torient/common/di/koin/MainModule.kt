package app.shynline.torient.common.di.koin

import app.shynline.torient.common.di.viewfactory.ViewMvcFactory
import app.shynline.torient.common.di.viewfactory.ViewMvcFactoryImpl
import app.shynline.torient.torrent.mediator.SubscriptionMediator
import app.shynline.torient.torrent.mediator.TorrentMediator
import app.shynline.torient.torrent.torrent.Torrent
import app.shynline.torient.torrent.torrent.TorrentImpl
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val mainModule = module {
    single<ViewMvcFactory> {
        ViewMvcFactoryImpl()
    }
    single<Torrent> {
        TorrentImpl(androidContext(), get(), get())
    }
    single {
        SubscriptionMediator(get())
    }
    single {
        Dispatchers.IO
    }
    single {
        TorrentMediator(get())
    }
}