package app.shynline.torient.domain.database.datasource.torrent

import app.shynline.torient.domain.database.common.states.TorrentUserState
import app.shynline.torient.domain.database.dao.TorrentDao
import app.shynline.torient.domain.database.entities.TorrentSchema
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
            // Returns true if both lists are equivalent
            val o = old.map { it.infoHash }.toMutableList()
            new.map { it.infoHash }.forEach {
                if (o.contains(it)) {
                    o.remove(it)
                } else {
                    // There is at least one new object in new list
                    return@distinctUntilChanged false
                }
            }
            // There is at least on object which is not in new list
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


    override suspend fun getTorrent(infoHash: String): TorrentSchema? =
        withContext(ioDispatcher) {
            return@withContext torrentDao.getTorrent(infoHash)
        }

    override suspend fun removeTorrent(infoHash: String) =
        withContext(ioDispatcher) {
            torrentDao.removeTorrent(infoHash)
        }
}