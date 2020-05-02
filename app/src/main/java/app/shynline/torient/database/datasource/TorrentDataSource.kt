package app.shynline.torient.database.datasource

import app.shynline.torient.database.TorrentState
import app.shynline.torient.database.entities.TorrentSchema
import kotlinx.coroutines.flow.Flow

interface TorrentDataSource {
    suspend fun getTorrents(): Flow<List<TorrentSchema>>
    suspend fun insertTorrent(torrentSchema: TorrentSchema)
    suspend fun getTorrentState(infoHash: String): TorrentState
    suspend fun setTorrentState(infoHash: String, state: TorrentState)
    suspend fun setTorrentFinished(infoHash: String, finished: Boolean)
    suspend fun setTorrentProgress(infoHash: String, progress: Float)
}