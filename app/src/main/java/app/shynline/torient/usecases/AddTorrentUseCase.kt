package app.shynline.torient.usecases

import app.shynline.torient.model.TorrentIdentifier
import app.shynline.torient.torrent.torrent.Torrent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AddTorrentUseCase(
    private val torrent: Torrent
) {
    suspend fun execute(identifier: TorrentIdentifier) = withContext(Dispatchers.IO) {
        torrent.addTorrent(identifier)
    }
}