package app.shynline.torient.database.datasource

import app.shynline.torient.database.TorrentDao
import app.shynline.torient.database.entities.TorrentSchema
import app.shynline.torient.database.states.TorrentUserState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext

class TorrentDataSourceImpl(
    private val torrentDao: TorrentDao,
    private val ioDispatcher: CoroutineDispatcher
) : TorrentDataSource {


    @FlowPreview
    override suspend fun getTorrents(): Flow<List<TorrentSchema>> = withContext(ioDispatcher) {
        return@withContext torrentDao.getTorrents().distinctUntilChanged { old, new ->
            val o = old.map { it.infoHash }.toMutableList()
            new.map { it.infoHash }.forEach {
                if (o.contains(it)) {
                    o.remove(it)
                } else {
                    return@distinctUntilChanged false
                }
            }
            o.isEmpty()
        }
    }

    override suspend fun insertTorrent(torrentSchema: TorrentSchema) = withContext(ioDispatcher) {
        torrentDao.insertTorrent(torrentSchema)
    }

    override suspend fun getTorrentState(infoHash: String): TorrentUserState =
        withContext(ioDispatcher) {
            return@withContext torrentDao.getTorrentState(infoHash)
        }

    override suspend fun setTorrentState(infoHash: String, userState: TorrentUserState) =
        withContext(ioDispatcher) {
            return@withContext torrentDao.setTorrentState(infoHash, userState)
        }

    override suspend fun setTorrentFinished(infoHash: String, finished: Boolean) =
        withContext(ioDispatcher) {
            return@withContext torrentDao.setTorrentFinished(infoHash, finished)
        }

    override suspend fun setTorrentProgress(infoHash: String, progress: Float) =
        withContext(ioDispatcher) {
            return@withContext torrentDao.setTorrentProgress(infoHash, progress)
        }

    override suspend fun getTorrent(infoHash: String): TorrentSchema? =
        withContext(ioDispatcher) {
            return@withContext torrentDao.getTorrent(infoHash)
        }
}