package app.shynline.torient.model

import app.shynline.torient.database.states.TorrentUserState
import app.shynline.torient.torrent.states.ManageState
import app.shynline.torient.torrent.states.TorrentDownloadingState
import com.frostwire.jlibtorrent.TorrentInfo

data class TorrentDetail(
    val infoHash: String,
    val name: String,
    val author: String,
    val comment: String,
    val totalSize: Long,
    val torrentFile: TorrentFile,
    val magnet: String,
    val hexHash: Long
) {
    var serviceState: ManageState = ManageState.UNKNOWN
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
                torrentInfo.creator(),
                torrentInfo.comment(),
                torrentInfo.totalSize(),
                TorrentFile.from(torrentInfo),
                torrentInfo.makeMagnetUri(),
                torrentInfo.infoHash().toHex().hashCode().toLong()
            )
        }
    }
}