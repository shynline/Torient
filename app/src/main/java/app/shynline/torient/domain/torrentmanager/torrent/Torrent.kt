package app.shynline.torient.domain.torrentmanager.torrent

import app.shynline.torient.domain.models.TorrentFilePriority
import app.shynline.torient.domain.models.TorrentIdentifier
import app.shynline.torient.domain.models.TorrentModel
import app.shynline.torient.domain.models.TorrentOverview
import app.shynline.torient.domain.torrentmanager.common.events.TorrentEvent
import app.shynline.torient.utils.observable.Observable
import com.frostwire.jlibtorrent.TorrentHandle

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
    suspend fun getTorrentOverview(infoHash: String): TorrentOverview?
    fun isTorrentFileCached(infoHash: String): Boolean
    fun updateTorrentPreference(infoHash: String)
    fun onUpdateGlobalPreference()
    suspend fun setFilePriority(
        infoHash: String,
        index: Int,
        torrentFilePriority: TorrentFilePriority,
        torrentHandle: TorrentHandle? = null
    )

    suspend fun setFilesPriority(infoHash: String, torrentFilePriorities: List<TorrentFilePriority>)

    /**
     * Remove a torrent from service and cache
     *
     * @param infoHash
     * @return true if there is any torrent to be removed false otherwise
     */
    suspend fun removeTorrent(infoHash: String): Boolean
    suspend fun removeTorrentFiles(name: String): Boolean

}
