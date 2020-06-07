package app.shynline.torient.domain.database.datasource.torrentfilepriority

import app.shynline.torient.domain.database.entities.TorrentFilePrioritySchema

interface TorrentFilePriorityDataSource {
    suspend fun setPriority(schema: TorrentFilePrioritySchema)
    suspend fun getPriority(infoHash: String): TorrentFilePrioritySchema
    suspend fun removeTorrentFilePriority(infoHash: String)
}