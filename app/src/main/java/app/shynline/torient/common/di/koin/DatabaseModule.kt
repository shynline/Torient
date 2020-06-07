package app.shynline.torient.common.di.koin

import androidx.room.Room
import app.shynline.torient.domain.database.TorrentDataBase
import app.shynline.torient.domain.database.datasource.torrent.InternalTorrentDataSource
import app.shynline.torient.domain.database.datasource.torrent.InternalTorrentDataSourceImpl
import app.shynline.torient.domain.database.datasource.torrent.TorrentDataSource
import app.shynline.torient.domain.database.datasource.torrent.TorrentDataSourceImpl
import app.shynline.torient.domain.database.datasource.torrentfilepriority.TorrentFilePriorityDataSource
import app.shynline.torient.domain.database.datasource.torrentfilepriority.TorrentFilePriorityDataSourceImpl
import app.shynline.torient.domain.database.datasource.torrentpreference.TorrentPreferenceDataSource
import app.shynline.torient.domain.database.datasource.torrentpreference.TorrentPreferenceDataSourceImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(androidContext(), TorrentDataBase::class.java, "torient.db").build()
    }

    single {
        get<TorrentDataBase>().torrentFilePriorityDao()
    }

    single {
        get<TorrentDataBase>().torrentDao()
    }

    single {
        get<TorrentDataBase>().torrentPreferenceDao()
    }

    single<TorrentPreferenceDataSource> {
        TorrentPreferenceDataSourceImpl(get(), get(named("io")))
    }

    single<TorrentDataSource> {
        TorrentDataSourceImpl(
            get(),
            get(named("io"))
        )
    }

    single<InternalTorrentDataSource> {
        InternalTorrentDataSourceImpl(
            get(),
            get(named("io"))
        )
    }

    single<TorrentFilePriorityDataSource> {
        TorrentFilePriorityDataSourceImpl(
            get(),
            get(named("io"))
        )
    }
}
