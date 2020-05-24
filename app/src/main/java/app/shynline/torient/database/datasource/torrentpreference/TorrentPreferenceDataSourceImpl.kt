package app.shynline.torient.database.datasource.torrentpreference

import app.shynline.torient.database.dao.TorrentPreferenceDao
import app.shynline.torient.database.entities.TorrentPreferenceSchema
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TorrentPreferenceDataSourceImpl(
    private val torrentPreferenceDao: TorrentPreferenceDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TorrentPreferenceDataSource {

    override suspend fun getTorrentPreference(infoHash: String): TorrentPreferenceSchema =
        withContext(ioDispatcher) {
            var schema = torrentPreferenceDao.getPreference(infoHash)
            if (schema == null) {
                schema = TorrentPreferenceSchema(infoHash)
                torrentPreferenceDao.insertPreference(schema)
            }
            return@withContext schema
        }

    override suspend fun updateTorrentPreference(schema: TorrentPreferenceSchema) =
        withContext(ioDispatcher) {
            torrentPreferenceDao.updateSchema(schema)
            Unit
        }

}