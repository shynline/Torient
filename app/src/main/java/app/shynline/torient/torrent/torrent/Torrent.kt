package app.shynline.torient.torrent.torrent

import app.shynline.torient.TorrentDetail
import app.shynline.torient.TorrentIdentifier

interface Torrent {
    fun getTorrentIdentifier(data: ByteArray): TorrentIdentifier
    fun getTorrentDetail(infoHash: String): TorrentDetail?
    fun getTorrentDetail(identifier: TorrentIdentifier): TorrentDetail?
}