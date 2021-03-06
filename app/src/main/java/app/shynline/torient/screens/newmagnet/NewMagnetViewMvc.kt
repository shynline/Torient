package app.shynline.torient.screens.newmagnet

import app.shynline.torient.screens.common.view.ObservableViewMvc
import app.shynline.torient.domain.torrentmanager.utils.Magnet

interface NewMagnetViewMvc : ObservableViewMvc<NewMagnetViewMvc.Listener> {

    interface Listener {
        fun onDownloadClicked()
    }

    fun showMagnet(magnet: Magnet)

}