package app.shynline.torient.utils.observable

import java.util.*

/**
 * A base generic class implementing the observable interface
 *
 * @param LISTENER
 */
abstract class BaseObservable<LISTENER> :
    Observable<LISTENER> {
    private val listeners: MutableSet<LISTENER> = hashSetOf()

    /**
     * register a listener
     *
     * @param listener
     */
    final override fun registerListener(listener: LISTENER) {
        listeners.add(listener)
    }

    /**
     * unregister a listener
     *
     * @param listener
     */
    final override fun unRegisterListener(listener: LISTENER) {
        listeners.remove(listener)
    }

    /**
     * get all listeners as unmodifiable set
     *
     * @return Set<LISTENER>
     */
    fun getListeners(): Set<LISTENER> = Collections.unmodifiableSet(listeners)
}