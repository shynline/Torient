package app.shynline.torient.domain.database.datasource.torrentpreference

import app.shynline.torient.domain.database.entities.TorrentPreferenceSchema

interface TorrentPreferenceDataSource {
    suspend fun getTorrentPreference(infoHash: String): TorrentPreferenceSchema
    suspend fun updateTorrentPreference(schema: TorrentPreferenceSchema)
}