package app.shynline.torient.model

import com.frostwire.jlibtorrent.TorrentInfo

data class TorrentIdentifier(
    val infoHash: String,
    val magnet: String
) {
    companion object {
        fun from(torrentInfo: TorrentInfo): TorrentIdentifier {
            return TorrentIdentifier(
                torrentInfo.infoHash().toHex(),
                torrentInfo.makeMagnetUri()
            )
        }
    }
}