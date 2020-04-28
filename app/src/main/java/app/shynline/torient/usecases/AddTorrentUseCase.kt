package app.shynline.torient.usecases

import app.shynline.torient.torrent.torrent.Torrent

class AddTorrentUseCase(
    private val torrent: Torrent
) {
    fun execute(magnet: String) {
        torrent.downloadTorrent(magnet)
    }
}