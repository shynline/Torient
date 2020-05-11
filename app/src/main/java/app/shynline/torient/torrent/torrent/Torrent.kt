package app.shynline.torient.torrent.torrent

import app.shynline.torient.common.observable.Observable
import app.shynline.torient.model.TorrentIdentifier
import app.shynline.torient.model.TorrentModel
import app.shynline.torient.torrent.events.TorrentEvent

interface Torrent :
    Observable<Torrent.Listener> {
    interface Listener {
        fun onStatReceived(torrentEvent: TorrentEvent)
    }

    suspend fun getTorrentModel(data: ByteArray?): TorrentModel?
    suspend fun getTorrentModel(identifier: TorrentIdentifier): TorrentModel?
    suspend fun getTorrentModel(magnet: String): TorrentModel?
    suspend fun getTorrentModelFromInfoHash(infoHash: String): TorrentModel?
    suspend fun addTorrent(identifier: TorrentIdentifier)
    suspend fun getAllManagedTorrents(): List<String>
    fun isTorrentFileCached(infoHash: String): Boolean

    /**
     * Remove a torrent from service and cache
     *
     * @param infoHash
     * @return true if there is any torrent to be removed false otherwise
     */
    suspend fun removeTorrent(infoHash: String): Boolean
    suspend fun removeTorrentFiles(name: String): Boolean

}
