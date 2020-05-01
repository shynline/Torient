package app.shynline.torient.usecases

import app.shynline.torient.model.TorrentDetail
import app.shynline.torient.model.TorrentIdentifier
import app.shynline.torient.torrent.torrent.Torrent

class GetTorrentDetailUseCase(
    private val torrent: Torrent
) {

    suspend fun execute(
        infoHash: String? = null,
        identifier: TorrentIdentifier? = null,
        torrentFile: ByteArray? = null
    ): TorrentDetail? {
        infoHash?.let {
            return torrent.getTorrentDetailFromInfoHash(it)
        }
        torrentFile?.let {
            return torrent.getTorrentDetail(it)
        }
        return torrent.getTorrentDetail(identifier!!)
    }
}