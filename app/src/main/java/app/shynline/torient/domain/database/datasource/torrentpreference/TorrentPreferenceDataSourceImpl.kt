package app.shynline.torient.domain.database.datasource.torrentpreference

import app.shynline.torient.domain.database.dao.TorrentPreferenceDao
import app.shynline.torient.domain.database.entities.TorrentPreferenceSchema
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class TorrentPreferenceDataSourceImpl(
    private val torrentPreferenceDao: TorrentPreferenceDao,
    private val ioDispatcher: CoroutineDispatcher
) : TorrentPreferenceDataSource {

    override suspend fun getTorrentPreference(infoHash: String): TorrentPreferenceSchema =
        withContext(ioDispatcher) {
            var schema = torrentPreferenceDao.getPreference(infoHash)
            if (schema == null) {
                schema = TorrentPreferenceSchema(infoHash)
                torrentPreferenceDao.insertPreference(schema)
            }
            return@withContext requireNotNull(schema)
        }

    override suspend fun updateTorrentPreference(schema: TorrentPreferenceSchema) =
        withContext(ioDispatcher) {
            torrentPreferenceDao.updateSchema(schema)
            Unit
        }

}