package app.shynline.torient.torrent.torrent

import app.shynline.torient.common.observable.Observable
import app.shynline.torient.model.TorrentDetail
import app.shynline.torient.model.TorrentEvent
import app.shynline.torient.model.TorrentIdentifier

interface Torrent :
    Observable<Torrent.Listener> {
    interface Listener {
        fun onStatReceived(torrentEvent: TorrentEvent)
    }

    suspend fun getTorrentDetail(data: ByteArray?): TorrentDetail?
    suspend fun getTorrentDetail(identifier: TorrentIdentifier): TorrentDetail?
    suspend fun getTorrentDetail(magnet: String): TorrentDetail?
    suspend fun addTorrent(magnet: String)
    suspend fun addTorrent(identifier: TorrentIdentifier)
    suspend fun resumeTorrent(infoHash: String)
    suspend fun pauseTorrent(infoHash: String)

    /**
     * remove a torrent from service
     *
     * @param infoHash the torrent's infoHash
     * @return false if such infoHash doesn't exist (probably already removed) otherwise true
     */
    suspend fun removeTorrent(infoHash: String): Boolean

}
