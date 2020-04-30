package app.shynline.torient.database

enum class TorrentState(val id: Int) {
    PAUSED(0),
    FINISHED(1),
    ACTIVE(2)
}