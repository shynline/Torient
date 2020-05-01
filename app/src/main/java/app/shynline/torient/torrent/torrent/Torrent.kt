package app.shynline.torient.torrent.torrent

import app.shynline.torient.common.observable.Observable
import app.shynline.torient.model.TorrentDetail
import app.shynline.torient.model.TorrentIdentifier
import app.shynline.torient.torrent.TorrentEvent

interface Torrent :
    Observable<Torrent.Listener> {
    interface Listener {
        fun onStatReceived(torrentEvent: TorrentEvent)
    }

    suspend fun getTorrentDetail(data: ByteArray?): TorrentDetail?
    suspend fun getTorrentDetail(identifier: TorrentIdentifier): TorrentDetail?
    suspend fun getTorrentDetail(magnet: String): TorrentDetail?
    suspend fun getTorrentDetailFromInfoHash(infoHash: String): TorrentDetail?
    suspend fun addTorrent(identifier: TorrentIdentifier)
    suspend fun resumeTorrent(infoHash: String): Boolean
    suspend fun pauseTorrent(infoHash: String): Boolean
    suspend fun getManagedTorrentState(infoHash: String): ManageState?
    suspend fun getAllManagedTorrentStats(): Map<String, ManageState>

    /**
     * Remove a torrent from service and cache
     *
     * @param infoHash
     * @return true if there is any torrent to be removed false otherwise
     */
    suspend fun removeTorrent(infoHash: String): Boolean

}
