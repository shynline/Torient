package app.shynline.torient.model

import app.shynline.torient.database.states.TorrentUserState
import app.shynline.torient.torrent.states.TorrentDownloadingState
import com.frostwire.jlibtorrent.TorrentInfo

data class TorrentDetail(
    val infoHash: String,
    var name: String,
    var magnet: String
) {
    var hexHash: Long = 0L
    var author: String = ""
    var comment: String = ""
    var totalSize: Long = 0L
    var torrentFile: TorrentFile? = null
    var userState: TorrentUserState = TorrentUserState.PAUSED
    var downloadingState: TorrentDownloadingState = TorrentDownloadingState.UNKNOWN
    var progress = 0f
    var downloadRate = 0
    var uploadRate = 0
    var finished = false
    var maxPeers = 0
    var connectedPeers = 0
    fun toIdentifier(): TorrentIdentifier {
        return TorrentIdentifier(
            infoHash,
            magnet
        )
    }

    companion object {
        fun from(torrentInfo: TorrentInfo): TorrentDetail {
            return TorrentDetail(
                torrentInfo.infoHash().toHex(),
                torrentInfo.name(),
                torrentInfo.makeMagnetUri()
            ).apply {
                comment = torrentInfo.comment()
                totalSize = torrentInfo.totalSize()
                torrentFile = TorrentFile.from(torrentInfo)
                author = torrentInfo.creator()
                hexHash = torrentInfo.infoHash().toHex().hashCode().toLong()
            }
        }
    }
}