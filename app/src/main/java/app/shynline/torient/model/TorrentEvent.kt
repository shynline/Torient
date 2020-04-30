package app.shynline.torient.model

sealed class TorrentEvent(val infoHash: String)
class AddTorrentEvent(infoHash: String, val succeed: Boolean) : TorrentEvent(infoHash)
class TorrentResumedEvent(infoHash: String) : TorrentEvent(infoHash)
class TorrentPausedEvent(infoHash: String) : TorrentEvent(infoHash)
class TorrentFinishedEvent(infoHash: String) : TorrentEvent(infoHash)
class TorrentRemovedEvent(infoHash: String) : TorrentEvent(infoHash)