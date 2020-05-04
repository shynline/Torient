package app.shynline.torient.usecases

import app.shynline.torient.torrent.states.ManageState
import app.shynline.torient.torrent.torrent.Torrent

class GetAllManagedTorrentStatesUseCase(
    private val torrent: Torrent
) {
    suspend fun execute(): Map<String, ManageState> {
        return torrent.getAllManagedTorrentStats()
    }
}