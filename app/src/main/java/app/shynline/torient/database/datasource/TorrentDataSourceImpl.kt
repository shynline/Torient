package app.shynline.torient.database.datasource

import app.shynline.torient.database.TorrentDao
import app.shynline.torient.database.TorrentState
import app.shynline.torient.database.entities.TorrentSchema
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TorrentDataSourceImpl(
    private val torrentDao: TorrentDao,
    private val ioDispatcher: CoroutineDispatcher
) : TorrentDataSource {

    override suspend fun getTorrents(): Flow<List<TorrentSchema>> = withContext(ioDispatcher) {
        return@withContext torrentDao.getTorrents()
    }

    override suspend fun insertTorrent(torrentSchema: TorrentSchema) = withContext(ioDispatcher) {
        torrentDao.insertTorrent(torrentSchema)
    }

    override suspend fun getTorrentState(infoHash: String): TorrentState =
        withContext(ioDispatcher) {
            return@withContext torrentDao.getTorrentState(infoHash)
        }
}