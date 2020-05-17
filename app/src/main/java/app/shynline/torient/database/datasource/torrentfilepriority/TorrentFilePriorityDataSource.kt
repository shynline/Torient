package app.shynline.torient.database.datasource.torrentfilepriority

import app.shynline.torient.database.entities.TorrentFilePrioritySchema

interface TorrentFilePriorityDataSource {
    suspend fun setPriority(schema: TorrentFilePrioritySchema)
    suspend fun getPriority(infoHash: String): TorrentFilePrioritySchema
}