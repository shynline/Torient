package dataset

import app.shynline.torient.domain.database.entities.TorrentPreferenceSchema

object TorrentPreferenceUtils {
    fun getSchema(): TorrentPreferenceSchema {
        return TorrentPreferenceSchema("infoHash")
    }
}