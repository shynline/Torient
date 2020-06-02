package app.shynline.torient.common.di.koin

import app.shynline.torient.torrent.mediator.usecases.*
import org.koin.dsl.module

val useCaseModule = module {
    single {
        InitiateFilePriorityUseCase(
            get()
        )
    }
    single {
        GetTorrentModelUseCase(get())
    }
    single {
        AddTorrentToDataBaseUseCase(
            get(),
            get()
        )
    }
    single {
        GetTorrentFilePriorityUseCase(get())
    }
    single {
        GetTorrentSchemeUseCase(get())
    }
    single {
        UpdateTorrentFilePriorityUseCase(get(), get())
    }
}