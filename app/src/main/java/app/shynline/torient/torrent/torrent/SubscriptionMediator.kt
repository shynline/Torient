package app.shynline.torient.torrent.torrent

import app.shynline.torient.model.TorrentStats

class SubscriptionMediator(
    private val torrent: Torrent
) : Torrent.Listener {
    interface Listener {
        fun onStatReceived(torrentStats: TorrentStats)
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
        listeners[listener]?.add(torrent)
    }

    fun removeTorrent(listener: Listener, torrent: String) {
        listeners[listener]?.remove(torrent)
    }

    override fun onStatReceived(torrentStats: TorrentStats) {
        listeners.forEach {
            if (it.value.contains(torrentStats.infoHash)) {
                it.key.onStatReceived(torrentStats)
            }
        }
    }
}