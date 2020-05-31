package app.shynline.torient.common.di.koin

import app.shynline.torient.torrent.mediator.usecases.AddTorrentToDataBaseUseCase
import app.shynline.torient.torrent.mediator.usecases.GetTorrentModelUseCase
import app.shynline.torient.torrent.mediator.usecases.InitiateFilePriorityUseCase
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
}