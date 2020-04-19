package app.shynline.torient.torrent.internal.announce

internal interface AnnounceListener {
    fun announce(timeFrameInSec: Int)
}