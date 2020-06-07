package app.shynline.torient.domain.mediator

import app.shynline.torient.domain.models.TorrentFilePriority
import app.shynline.torient.domain.models.TorrentIdentifier
import app.shynline.torient.domain.models.TorrentModel
import app.shynline.torient.domain.models.TorrentOverview
import app.shynline.torient.domain.torrentmanager.torrent.Torrent
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

    fun updateTorrentPreference(infoHash: String) {
        torrent.updateTorrentPreference(infoHash)
    }

    fun onUpdateGlobalPreference() {
        torrent.onUpdateGlobalPreference()
    }
}