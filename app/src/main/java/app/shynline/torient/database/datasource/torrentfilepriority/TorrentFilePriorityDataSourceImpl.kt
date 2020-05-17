package app.shynline.torient.database.datasource.torrentfilepriority

import app.shynline.torient.database.TorrentFilePriorityDao
import app.shynline.torient.database.entities.TorrentFilePrioritySchema
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class TorrentFilePriorityDataSourceImpl(
    private val torrentFilePriorityDao: TorrentFilePriorityDao,
    private val ioDispatcher: CoroutineDispatcher
) : TorrentFilePriorityDataSource {

    override suspend fun setPriority(schema: TorrentFilePrioritySchema) =
        withContext(ioDispatcher) {
            val rows = torrentFilePriorityDao.setTorrentFilePriorities(
                schema.infoHash,
                schema.filePriority
            )
            if (rows == 0) {
                torrentFilePriorityDao.insertTorrent(schema)
            }
        }

    override suspend fun getPriority(infoHash: String): TorrentFilePrioritySchema {
        var schema = torrentFilePriorityDao.getTorrentFilePrioritySchema(infoHash)
        if (schema == null) {
            schema = TorrentFilePrioritySchema(infoHash)
            torrentFilePriorityDao.insertTorrent(schema)
        }
        return schema
    }
}