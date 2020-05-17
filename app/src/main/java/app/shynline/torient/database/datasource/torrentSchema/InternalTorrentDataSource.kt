package app.shynline.torient.database.datasource.torrentSchema

interface InternalTorrentDataSource {
    suspend fun setTorrentFinished(infoHash: String, finished: Boolean)
    suspend fun setTorrentProgress(
        infoHash: String,
        progress: Float,
        lastSeenComplete: Long,
        fileProgress: LongArray
    )
}