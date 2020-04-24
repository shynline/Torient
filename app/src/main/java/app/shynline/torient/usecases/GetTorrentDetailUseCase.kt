package app.shynline.torient.usecases

import app.shynline.torient.scheme.TorrentDetail
import app.shynline.torient.scheme.TorrentIdentifier
import app.shynline.torient.torrent.torrent.Torrent

class GetTorrentDetailUseCase(
    private val torrent: Torrent
) {

    fun execute(infoHash: String? = null, identifier: TorrentIdentifier? = null): TorrentDetail? {
        infoHash?.let {
            return torrent.getTorrentDetail(it)
        }
        return torrent.getTorrentDetail(identifier!!)
    }
}