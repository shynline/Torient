package dataset

import app.shynline.torient.domain.models.TorrentModel

object TorrentModelUtils {
    fun getTorrentModel(infoHash: String, name: String, magnet: String): TorrentModel {
        return TorrentModel(infoHash, name, magnet)
    }
}