package app.shynline.torient.model

import com.frostwire.jlibtorrent.TorrentInfo

data class TorrentDetail(
    val infoHash: String,
    val name: String,
    val author: String,
    val comment: String,
    val totalSize: Long,
    val torrentFile: TorrentFile
) {

    companion object {
        fun from(torrentInfo: TorrentInfo): TorrentDetail {
            return TorrentDetail(
                torrentInfo.infoHash().toHex(),
                torrentInfo.name(),
                torrentInfo.creator(),
                torrentInfo.comment(),
                torrentInfo.totalSize(),
                TorrentFile.from(torrentInfo)
            )
        }
    }
}