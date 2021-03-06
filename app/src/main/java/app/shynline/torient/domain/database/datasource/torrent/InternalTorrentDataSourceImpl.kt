package app.shynline.torient.domain.database.datasource.torrent

import app.shynline.torient.domain.database.common.typeconverter.LongArrayConverter
import app.shynline.torient.domain.database.dao.TorrentDao
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
            torrentDao.setTorrentProgress(infoHash, 100f)
        }

    override suspend fun setTorrentProgress(
        infoHash: String,
        progress: Float,
        lastSeenComplete: Long,
        fileProgress: LongArray
    ) =
        withContext(ioDispatcher) {
            torrentDao.setTorrentProgress(infoHash, progress)

            torrentDao.setTorrentLastSeenComplete(infoHash, lastSeenComplete)

            torrentDao.setTorrentFileProgress(
                infoHash,
                LongArrayConverter.toString(fileProgress.toList())!!
            )
        }
}