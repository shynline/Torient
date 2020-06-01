package app.shynline.torient.screens.common.view

import java.util.*

abstract class BaseObservableViewMvc<LISTENER> : BaseViewMvc(), ObservableViewMvc<LISTENER> {

    private val listeners: MutableSet<LISTENER> = hashSetOf()

    final override fun registerListener(listener: LISTENER) {
        listeners.add(listener)
        onListenerRegistered()
    }

    final override fun unRegisterListener(listener: LISTENER) {
        listeners.remove(listener)
        onListenerUnRegistered()
    }

    protected open fun onListenerRegistered() {}
    protected open fun onListenerUnRegistered() {}

    fun getListeners(): Set<LISTENER> = Collections.unmodifiableSet(listeners)
}