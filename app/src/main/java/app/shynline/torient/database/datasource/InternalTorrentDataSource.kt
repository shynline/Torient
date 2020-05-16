package app.shynline.torient.database.datasource

interface InternalTorrentDataSource {
    suspend fun setTorrentFinished(infoHash: String, finished: Boolean)
    suspend fun setTorrentProgress(infoHash: String, progress: Float, lastSeenComplete: Long)
}