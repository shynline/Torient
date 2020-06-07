package app.shynline.torient.utils.observable

/**
 * It's a generic Observable interface
 *
 * @param LISTENER is the type of the listener which will be observed
 */
interface Observable<LISTENER> {
    /**
     * add a listener
     *
     * @param listener
     */
    fun registerListener(listener: LISTENER)

    /**
     * remove a listener
     *
     * @param listener
     */
    fun unRegisterListener(listener: LISTENER)
}