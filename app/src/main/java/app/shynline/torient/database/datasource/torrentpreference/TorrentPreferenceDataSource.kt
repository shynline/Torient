package app.shynline.torient.database.datasource.torrentpreference

import app.shynline.torient.database.entities.TorrentPreferenceSchema

interface TorrentPreferenceDataSource {
    suspend fun getTorrentPreference(infoHash: String): TorrentPreferenceSchema
    suspend fun updateTorrentPreference(schema: TorrentPreferenceSchema)
}