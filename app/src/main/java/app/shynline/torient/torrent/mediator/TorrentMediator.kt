package app.shynline.torient.torrent.mediator

import app.shynline.torient.model.TorrentFilePriority
import app.shynline.torient.model.TorrentIdentifier
import app.shynline.torient.model.TorrentModel
import app.shynline.torient.model.TorrentOverview
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

    suspend fun getAllManagedTorrents(): List<String> {
        return torrent.getAllManagedTorrents()
    }

    suspend fun getTorrentModel(
        infoHash: String? = null,
        identifier: TorrentIdentifier? = null,
        torrentFile: ByteArray? = null,
        magnet: String? = null
    ): TorrentModel? {
        infoHash?.let {
            return torrent.getTorrentModelFromInfoHash(it)
        }
        torrentFile?.let {
            return torrent.getTorrentModel(it)
        }
        identifier?.let {
            return torrent.getTorrentModel(it)
        }
        return torrent.getTorrentModel(magnet!!)
    }

    suspend fun removeTorrent(infoHash: String): Boolean {
        return torrent.removeTorrent(infoHash)
    }

    fun isTorrentFileCached(infoHash: String): Boolean {
        return torrent.isTorrentFileCached(infoHash)
    }

    suspend fun removeTorrentFiles(name: String): Boolean {
        return torrent.removeTorrentFiles(name)
    }

    suspend fun torrentOverview(infoHash: String): TorrentOverview? {
        return torrent.getTorrentOverview(infoHash)
    }

    suspend fun setFilePriority(
        infoHash: String,
        index: Int,
        torrentFilePriority: TorrentFilePriority
    ) {
        return torrent.setFilePriority(infoHash, index, torrentFilePriority)
    }
}