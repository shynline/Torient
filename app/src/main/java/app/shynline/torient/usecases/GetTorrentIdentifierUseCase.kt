package app.shynline.torient.usecases

import app.shynline.torient.scheme.TorrentIdentifier
import app.shynline.torient.torrent.torrent.Torrent

class GetTorrentIdentifierUseCase(
    val torrent: Torrent
) {
    fun execute(torrentData: ByteArray): TorrentIdentifier {
        return torrent.getTorrentIdentifier(torrentData)
    }
}