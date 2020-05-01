package app.shynline.torient.common.di

import app.shynline.torient.usecases.*
import org.koin.dsl.module

val useCaseModule = module {
    single {
        GetTorrentDetailUseCase(get())
    }
    single {
        AddTorrentUseCase(get())
    }
    single {
        ResumeTorrentUseCase(get())
    }
    single {
        GetAllManagedTorrentStatesUseCase(get())
    }
    single {
        RemoveTorrentUseCase(get())
    }
    single {
        PauseTorrentUserCase(get())
    }
}