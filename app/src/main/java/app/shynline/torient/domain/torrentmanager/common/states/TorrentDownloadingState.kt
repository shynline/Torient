package app.shynline.torient.domain.torrentmanager.common.states

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