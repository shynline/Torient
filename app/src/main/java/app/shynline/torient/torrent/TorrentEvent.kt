package app.shynline.torient.torrent

import app.shynline.torient.torrent.torrent.TorrentDownloadingState

sealed class TorrentEvent(val infoHash: String)
class AddTorrentEvent(infoHash: String, val succeed: Boolean) : TorrentEvent(infoHash)
class TorrentResumedEvent(infoHash: String) : TorrentEvent(infoHash)
class TorrentFinishedEvent(infoHash: String) : TorrentEvent(infoHash)
class TorrentRemovedEvent(infoHash: String) : TorrentEvent(infoHash)
class TorrentProgressEvent(
    infoHash: String,
    val state: TorrentDownloadingState,
    val progress: Float = 0f,
    val downloadRate: Int = 0,
    val uploadRate: Int = 0
) : TorrentEvent(infoHash)