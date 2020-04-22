package app.shynline.torient.common.di

import app.shynline.torient.common.di.viewfactory.ViewMvcFactory
import app.shynline.torient.common.di.viewfactory.ViewMvcFactoryImpl
import app.shynline.torient.screens.torrentslist.TorrentsListController
import app.shynline.torient.screens.torrentslist.TorrentsListFragment
import org.koin.dsl.module

val mainModule = module {
    single<ViewMvcFactory> {
        ViewMvcFactoryImpl()
    }
    scope<TorrentsListFragment> {
        scoped {
            TorrentsListController()
        }
    }

}