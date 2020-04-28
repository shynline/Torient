package app.shynline.torient.torrent.torrent

import app.shynline.torient.model.TorrentDetail
import app.shynline.torient.model.TorrentIdentifier
import app.shynline.torient.model.TorrentStats
import app.shynline.torient.torrent.service.Observable

interface Torrent : Observable<Torrent.Listener> {
    interface Listener {
        fun onStatReceived(torrentStats: TorrentStats)
    }
    fun getTorrentIdentifier(data: ByteArray): TorrentIdentifier
    fun getTorrentDetail(infoHash: String): TorrentDetail?
    fun getTorrentDetail(identifier: TorrentIdentifier): TorrentDetail?
    fun downloadTorrent(magnet: String)
}
