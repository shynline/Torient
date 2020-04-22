package app.shynline.torient.screens.common.view

interface ObservableViewMvc<LISTENER> : ViewMvc {
    fun registerListener(listener: LISTENER)
    fun unRegisterListener(listener: LISTENER)
}