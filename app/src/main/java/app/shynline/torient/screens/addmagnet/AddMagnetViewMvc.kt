package app.shynline.torient.screens.addmagnet

import app.shynline.torient.screens.common.view.ObservableViewMvc

interface AddMagnetViewMvc : ObservableViewMvc<AddMagnetViewMvc.Listener> {
    interface Listener {
        fun addMagnet(magnetStr: String)
        fun pasteClipBoard()
    }

    fun showClipBoard(string: String)
    fun showError(error: String)
}