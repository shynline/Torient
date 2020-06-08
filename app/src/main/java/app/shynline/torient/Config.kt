package app.shynline.torient

object Config {
    const val baseDownloadDir = "download"
    const val baseTorrentDir = "torrents"

    const val DEFAULT_FILE_BUFFER_SIZE = 8 * 1024
    const val DEFAULT_COPY_FILE_CHECKPOINT_SIZE = 512 * 1024

    const val TORRENT_RESET_BACKOFF_PERIOD = 5 * 60 * 1000 // 5 Minutes
    const val TORRENT_MIN_INACTIVITY = 1 * 60 * 1000 // 1 minute
    const val TORRENT_ACTIVE_RATE_THRESHOLD = 100 // byte/sec
}