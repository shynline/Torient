package app.shynline.torient.common.di.koin

import android.content.Context
import android.content.SharedPreferences
import app.shynline.torient.common.di.viewfactory.ViewMvcFactory
import app.shynline.torient.common.di.viewfactory.ViewMvcFactoryImpl
import app.shynline.torient.common.userpreference.UserPreference
import app.shynline.torient.common.userpreference.UserPreferenceImpl
import app.shynline.torient.domain.helpers.timer.TimerController
import app.shynline.torient.domain.helpers.timer.TimerControllerImpl
import app.shynline.torient.domain.mediator.SubscriptionMediator
import app.shynline.torient.domain.mediator.TorrentMediator
import app.shynline.torient.domain.torrentmanager.service.ActivityCycle
import app.shynline.torient.domain.torrentmanager.service.SessionController
import app.shynline.torient.domain.torrentmanager.service.SessionControllerImpl
import app.shynline.torient.domain.torrentmanager.torrent.Torrent
import app.shynline.torient.domain.torrentmanager.torrent.TorrentImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val mainModule = module {
    single<ViewMvcFactory> {
        ViewMvcFactoryImpl()
    }
    single<Torrent> {
        TorrentImpl(androidContext(), get(named("io")), get(), get(), get(), get(), get())
    }
    single {
        SessionControllerImpl(androidContext())
    }
    single<ActivityCycle> {
        get<SessionControllerImpl>()
    }
    single<SessionController> {
        get<SessionControllerImpl>()
    }
    single {
        SubscriptionMediator(get())
    }
    single(named(name = "io")) {
        Dispatchers.IO
    }
    single<CoroutineDispatcher>(named(name = "main")) {
        Dispatchers.Main
    }
    single {
        TorrentMediator(get())
    }
    single<SharedPreferences> {
        androidContext().getSharedPreferences("user_preference", Context.MODE_PRIVATE)
    }
    single<UserPreference> {
        UserPreferenceImpl(get())
    }
    single<TimerController> {
        TimerControllerImpl()
    }
}