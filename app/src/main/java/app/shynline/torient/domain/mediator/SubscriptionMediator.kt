package app.shynline.torient.domain.mediator

import app.shynline.torient.domain.torrentmanager.common.events.TorrentEvent
import app.shynline.torient.domain.torrentmanager.torrent.Torrent

class SubscriptionMediator(
    private val torrent: Torrent
) : Torrent.Listener {
    interface Listener {
        fun onStatReceived(torrentEvent: TorrentEvent)
    }

    private val listeners: MutableMap<Listener, MutableList<String>> = hashMapOf()
    private var subscribed = false

    fun subscribe(listener: Listener, torrents: Array<String>) {
        listeners[listener] = torrents.toMutableList()
        if (!subscribed) {
            torrent.registerListener(this)
            subscribed = true
        }
    }

    fun unsubscribe(listener: Listener) {
        listeners.remove(listener)
        if (listeners.isEmpty() && subscribed) {
            torrent.unRegisterListener(this)
            subscribed = false
        }
    }

    fun addTorrent(listener: Listener, torrent: String) {
        // Add the torrent if it doesn't exist
        if (listeners[listener]?.contains(torrent) == false)
            listeners[listener]?.add(torrent)
    }

    fun removeTorrent(listener: Listener, torrent: String) {
        listeners[listener]?.remove(torrent)
    }

    override fun onStatReceived(torrentEvent: TorrentEvent) {
        listeners.forEach {
            if (it.value.contains(torrentEvent.infoHash)) {
                it.key.onStatReceived(torrentEvent)
            }
        }
    }
}