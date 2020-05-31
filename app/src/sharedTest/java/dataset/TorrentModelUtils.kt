package dataset

import app.shynline.torient.model.TorrentModel

object TorrentModelUtils {
    fun getTorrentModel(infoHash: String, name: String, magnet: String): TorrentModel {
        return TorrentModel(infoHash, name, magnet)
    }
}