package app.shynline.torient.screens.common.view

import java.util.*

abstract class BaseObservableViewMvc<LISTENER> : ObservableViewMvc<LISTENER> {

    private val listeners: MutableSet<LISTENER> = hashSetOf()

    override fun registerListener(listener: LISTENER) {
        listeners.add(listener)
    }

    override fun unRegisterListener(listener: LISTENER) {
        listeners.remove(listener)
    }

    fun getListeners(): Set<LISTENER> = Collections.unmodifiableSet(listeners)
}