package app.shynline.torient.common.di

import androidx.room.Room
import app.shynline.torient.database.TorrentDataBase
import app.shynline.torient.database.datasource.TorrentDataSource
import app.shynline.torient.database.datasource.TorrentDataSourceImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(androidContext(), TorrentDataBase::class.java, "torient.db").build()
    }

    single {
        get<TorrentDataBase>().torrentDao()
    }

    single<TorrentDataSource> {
        TorrentDataSourceImpl(get(), get())
    }
}
