package app.shynline.torient.usecases

import app.shynline.torient.model.TorrentIdentifier
import app.shynline.torient.torrent.torrent.Torrent

class AddTorrentUseCase(
    private val torrent: Torrent
) {
    suspend fun execute(identifier: TorrentIdentifier) {
        torrent.addTorrent(identifier)
    }
}