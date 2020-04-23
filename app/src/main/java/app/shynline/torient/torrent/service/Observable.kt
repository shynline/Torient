package app.shynline.torient.torrent.service

interface Observable<LISTENER> {
    fun registerListener(listener: LISTENER)
    fun unRegisterListener(listener: LISTENER)
}