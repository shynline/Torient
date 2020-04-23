package app.shynline.torient.torrent.service

import android.app.Service
import java.util.*

abstract class ObservableService<LISTENER> : Service(), Observable<LISTENER> {

    private val listeners: MutableSet<LISTENER> = hashSetOf()

    final override fun registerListener(listener: LISTENER) {
        listeners.add(listener)
    }

    final override fun unRegisterListener(listener: LISTENER) {
        listeners.remove(listener)
    }

    fun getListeners(): Set<LISTENER> = Collections.unmodifiableSet(listeners)
}