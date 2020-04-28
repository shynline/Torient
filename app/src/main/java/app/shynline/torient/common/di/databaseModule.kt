package app.shynline.torient.common.di

import androidx.room.Room
import app.shynline.torient.database.TorrentDataBase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(androidContext(), TorrentDataBase::class.java, "torient.db").build()
    }

    single {
        get<TorrentDataBase>().torrentDao()
    }
}
