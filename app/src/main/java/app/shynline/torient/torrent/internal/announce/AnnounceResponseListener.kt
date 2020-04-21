package app.shynline.torient.torrent.internal.announce

import app.shynline.torient.torrent.Peer
import java.util.*


interface AnnounceResponseListener : EventListener {
    fun handleAnnounceResponse(interval: Int, complete: Int, incomplete: Int)
    fun handleDiscoveredPeers(peers: List<Peer>?)
}