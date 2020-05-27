package app.shynline.torient.torrent.events

import app.shynline.torient.model.TorrentModel
import app.shynline.torient.torrent.states.TorrentDownloadingState

sealed class TorrentEvent(val infoHash: String)
class TorrentProgressEvent(
    infoHash: String,
    val state: TorrentDownloadingState,
    val progress: Float = 0f,
    val downloadRate: Int = 0,
    val uploadRate: Int = 0,
    val maxPeers: Int = 0,
    val connectedPeers: Int = 0,
    val fileProgress: List<Long>? = null
) : TorrentEvent(infoHash)

class TorrentMetaDataEvent(infoHash: String, val torrentModel: TorrentModel) :
    TorrentEvent(infoHash)