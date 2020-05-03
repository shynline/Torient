package app.shynline.torient.database.datasource

import app.shynline.torient.database.TorrentUserState
import app.shynline.torient.database.entities.TorrentSchema
import kotlinx.coroutines.flow.Flow

interface TorrentDataSource {
    suspend fun getTorrents(): Flow<List<TorrentSchema>>
    suspend fun insertTorrent(torrentSchema: TorrentSchema)
    suspend fun getTorrentState(infoHash: String): TorrentUserState
    suspend fun setTorrentState(infoHash: String, userState: TorrentUserState)
    suspend fun setTorrentFinished(infoHash: String, finished: Boolean)
    suspend fun setTorrentProgress(infoHash: String, progress: Float)
}