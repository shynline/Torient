package app.shynline.torient.common

import android.app.Application
import android.util.Log
import app.shynline.torient.BuildConfig
import app.shynline.torient.common.di.koin.controllerModule
import app.shynline.torient.common.di.koin.databaseModule
import app.shynline.torient.common.di.koin.mainModule
import app.shynline.torient.common.di.koin.useCaseModule
import app.shynline.torient.domain.filetransfer.TransferService
import app.shynline.torient.domain.torrentmanager.service.TorientService
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level


class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TorientService.createNotificationChannel(this)
        TransferService.createNotificationChannel(this)
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@BaseApplication)
            modules(
                mainModule,
                databaseModule,
                controllerModule,
                useCaseModule
            )
        }
    }
}

fun logTorrent(str: String, secondaryTag: String? = null) {
    if (BuildConfig.DEBUG) {
        Log.d("torientTorrentLog", " $str${if (secondaryTag == null) "" else "-> $secondaryTag"}")
    }
}