package dataset

import app.shynline.torient.database.entities.TorrentPreferenceSchema

object TorrentPreferenceUtils {
    fun getSchema(): TorrentPreferenceSchema {
        return TorrentPreferenceSchema("infoHash")
    }
}