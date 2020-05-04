package app.shynline.torient.torrent.states

enum class TorrentDownloadingState {
    UNKNOWN,
    ALLOCATING,
    CHECKING_FILES,
    CHECKING_RESUME_DATA,
    DOWNLOADING,
    DOWNLOADING_METADATA,
    FINISHED,
    SEEDING
}