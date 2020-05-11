package app.shynline.torient.database.datasource

import app.shynline.torient.database.TorrentDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class InternalTorrentDataSourceImpl(
    private val torrentDao: TorrentDao,
    private val ioDispatcher: CoroutineDispatcher
) : InternalTorrentDataSource {

    override suspend fun setTorrentFinished(infoHash: String, finished: Boolean) =
        withContext(ioDispatcher) {
            return@withContext torrentDao.setTorrentFinished(infoHash, finished)
        }

    override suspend fun setTorrentProgress(infoHash: String, progress: Float) =
        withContext(ioDispatcher) {
            return@withContext torrentDao.setTorrentProgress(infoHash, progress)
        }
}