package app.shynline.torient.torrent

sealed class TorrentEvent(val infoHash: String)
class AddTorrentEvent(infoHash: String, val succeed: Boolean) : TorrentEvent(infoHash)
class TorrentResumedEvent(infoHash: String) : TorrentEvent(infoHash)
class TorrentPausedEvent(infoHash: String) : TorrentEvent(infoHash)
class TorrentFinishedEvent(infoHash: String) : TorrentEvent(infoHash)
class TorrentRemovedEvent(infoHash: String) : TorrentEvent(infoHash)
class TorrentProgressEvent(
    infoHash: String, val progress: Float, val downloadRate: Int, val uploadRate: Int
) : TorrentEvent(infoHash)