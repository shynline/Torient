package app.shynline.torient.database.datasource.torrent

interface InternalTorrentDataSource {
    suspend fun setTorrentFinished(infoHash: String, finished: Boolean, fileProgress: LongArray)
    suspend fun setTorrentProgress(
        infoHash: String,
        progress: Float,
        lastSeenComplete: Long,
        fileProgress: LongArray
    )
}