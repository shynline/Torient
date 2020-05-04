package app.shynline.torient.torrent.mediator

import app.shynline.torient.model.TorrentDetail
import app.shynline.torient.model.TorrentIdentifier
import app.shynline.torient.torrent.states.ManageState
import app.shynline.torient.torrent.torrent.Torrent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TorrentMediator(
    private val torrent: Torrent
) {
    suspend fun addTorrent(identifier: TorrentIdentifier) =
        withContext(Dispatchers.IO) {
            torrent.addTorrent(identifier)
        }

    suspend fun getAllManagedTorrentStates(): Map<String, ManageState> {
        return torrent.getAllManagedTorrentStats()
    }

    suspend fun getTorrentDetail(
        infoHash: String? = null,
        identifier: TorrentIdentifier? = null,
        torrentFile: ByteArray? = null
    ): TorrentDetail? {
        infoHash?.let {
            return torrent.getTorrentDetailFromInfoHash(it)
        }
        torrentFile?.let {
            return torrent.getTorrentDetail(it)
        }
        return torrent.getTorrentDetail(identifier!!)
    }

    suspend fun removeTorrent(infoHash: String): Boolean {
        return torrent.removeTorrent(infoHash)
    }
}