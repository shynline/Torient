package app.shynline.torient.torrent.internal.announce

import app.shynline.torient.torrent.Peer
import app.shynline.torient.torrent.internal.SharedTorrent
import app.shynline.torient.torrent.message.BaseMessage
import app.shynline.torient.torrent.message.BaseMessage.AnnounceRequestMessage.RequestEvent
import app.shynline.torient.torrent.message.BaseMessage.AnnounceResponseMessage
import app.shynline.torient.torrent.message.BaseMessage.ErrorMessage
import java.net.URI


abstract class TrackerClient(torrent: SharedTorrent, peer: Peer, tracker: URI) {
    private val listeners: MutableSet<AnnounceResponseListener>
    protected val torrent: SharedTorrent
    protected val peer: Peer
    val tracker: URI

    fun register(listener: AnnounceResponseListener) {
        listeners.add(listener)
    }

    fun unRegister(listener: AnnounceResponseListener) {
        listeners.remove(listener)
    }


    @Throws(AnnounceException::class)
    abstract fun announce(
        event: RequestEvent?,
        inhibitEvent: Boolean
    )

    protected open fun close() {
    }

    protected fun formatAnnounceEvent(
        event: RequestEvent
    ): String {
        return if (RequestEvent.NONE == event) "" else String.format(" %s", event.name)
    }

    @Throws(AnnounceException::class)
    protected open fun handleTrackerAnnounceResponse(
        message: BaseMessage,
        inhibitEvents: Boolean
    ) {
        if (message is ErrorMessage) {
            val error = message as ErrorMessage
            throw AnnounceException(error.getReason())
        }
        if (message !is AnnounceResponseMessage) {
            throw AnnounceException("Unexpected tracker message type " + message.type.name + "!")
        }
        if (inhibitEvents) {
            return
        }
        val response = message as AnnounceResponseMessage
        onAnnounceResponseEvent(
            response.getComplete(),
            response.getIncomplete(),
            response.getInterval()
        )
        onDiscoveredPeersEvent(
            response.getPeers()
        )
    }

    protected fun onAnnounceResponseEvent(
        complete: Int, incomplete: Int,
        interval: Int
    ) {
        for (listener in listeners) {
            listener.handleAnnounceResponse(interval, complete, incomplete)
        }
    }

    protected fun onDiscoveredPeersEvent(peers: List<Peer>?) {
        for (listener in listeners) {
            listener.handleDiscoveredPeers(peers)
        }
    }

    init {
        listeners = HashSet()
        this.torrent = torrent
        this.peer = peer
        this.tracker = tracker
    }
}