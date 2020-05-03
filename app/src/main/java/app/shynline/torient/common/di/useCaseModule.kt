package app.shynline.torient.common.di

import app.shynline.torient.usecases.AddTorrentUseCase
import app.shynline.torient.usecases.GetAllManagedTorrentStatesUseCase
import app.shynline.torient.usecases.GetTorrentDetailUseCase
import app.shynline.torient.usecases.RemoveTorrentUseCase
import org.koin.dsl.module

val useCaseModule = module {
    single {
        GetTorrentDetailUseCase(get())
    }
    single {
        AddTorrentUseCase(get())
    }
    single {
        GetAllManagedTorrentStatesUseCase(get())
    }
    single {
        RemoveTorrentUseCase(get())
    }
}