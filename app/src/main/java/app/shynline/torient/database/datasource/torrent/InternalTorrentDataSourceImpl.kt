package app.shynline.torient.database.datasource.torrent

import app.shynline.torient.database.common.typeconverter.LongArrayConverter
import app.shynline.torient.database.dao.TorrentDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class InternalTorrentDataSourceImpl(
    private val torrentDao: TorrentDao,
    private val ioDispatcher: CoroutineDispatcher
) : InternalTorrentDataSource {

    override suspend fun setTorrentFinished(
        infoHash: String,
        finished: Boolean,
        fileProgress: LongArray
    ) =
        withContext(ioDispatcher) {
            torrentDao.setTorrentFileProgress(
                infoHash,
                LongArrayConverter.toString(fileProgress.toList())!!
            )
            torrentDao.setTorrentFinished(infoHash, finished)
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