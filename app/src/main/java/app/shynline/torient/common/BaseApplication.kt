package app.shynline.torient.common

import android.app.Application
import app.shynline.torient.common.di.mainModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level


class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@BaseApplication)
            modules(mainModule)
        }
    }
}