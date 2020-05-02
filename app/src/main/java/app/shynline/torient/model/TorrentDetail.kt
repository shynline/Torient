package app.shynline.torient.model

import app.shynline.torient.database.TorrentState
import app.shynline.torient.torrent.torrent.ManageState
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

    var state: TorrentState = TorrentState.PAUSED
    var progress = 0f
    var downloadRate = 0
    var uploadRate = 0
    var finished = false
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