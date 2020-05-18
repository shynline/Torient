package app.shynline.torient.database.datasource.torrentSchema

import app.shynline.torient.database.TorrentDao
import app.shynline.torient.database.typeconverter.LongArrayConverter
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

    override suspend fun setTorrentProgress(
        infoHash: String,
        progress: Float,
        lastSeenComplete: Long,
        fileProgress: LongArray
    ) =
        withContext(ioDispatcher) {
            torrentDao.setTorrentProgress(
                infoHash,
                progress,
                lastSeenComplete
            )

            torrentDao.setTorrentFileProgress(
                infoHash,
                LongArrayConverter.toString(fileProgress.toList())!!
            )
        }
}