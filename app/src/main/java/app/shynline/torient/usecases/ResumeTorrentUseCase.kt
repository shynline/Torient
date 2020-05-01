package app.shynline.torient.usecases

import app.shynline.torient.torrent.torrent.Torrent

class ResumeTorrentUseCase(
    private val torrent: Torrent
) {
    suspend fun execute(infoHash: String): Boolean {
        return torrent.resumeTorrent(infoHash)
    }
}