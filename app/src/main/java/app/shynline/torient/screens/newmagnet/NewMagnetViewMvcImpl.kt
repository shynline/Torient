package app.shynline.torient.screens.newmagnet

import android.view.LayoutInflater
import android.view.ViewGroup
import app.shynline.torient.R
import app.shynline.torient.screens.common.view.BaseObservableViewMvc

class NewMagnetViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?
) : BaseObservableViewMvc<NewMagnetViewMvc.Listener>(), NewMagnetViewMvc {


    init {
        setRootView(inflater.inflate(R.layout.fragment_new_magnet, parent, false))

    }

}