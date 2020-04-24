package app.shynline.torient.common.di

import app.shynline.torient.usecases.GetTorrentDetailUseCase
import app.shynline.torient.usecases.GetTorrentIdentifierUseCase
import org.koin.dsl.module

val useCaseModule = module {
    single {
        GetTorrentIdentifierUseCase(get())
    }
    single {
        GetTorrentDetailUseCase(get())
    }
}