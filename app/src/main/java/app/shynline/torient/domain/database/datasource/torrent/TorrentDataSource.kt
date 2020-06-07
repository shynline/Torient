package app.shynline.torient.domain.database.datasource.torrent

import app.shynline.torient.domain.database.common.states.TorrentUserState
import app.shynline.torient.domain.database.entities.TorrentSchema
import kotlinx.coroutines.flow.Flow

interface TorrentDataSource {
    suspend fun getTorrents(): Flow<List<TorrentSchema>>
    suspend fun insertTorrent(torrentSchema: TorrentSchema)
    suspend fun getTorrentState(infoHash: String): TorrentUserState
    suspend fun setTorrentState(infoHash: String, userState: TorrentUserState)
    suspend fun getTorrent(infoHash: String): TorrentSchema?
    suspend fun removeTorrent(infoHash: String)
}