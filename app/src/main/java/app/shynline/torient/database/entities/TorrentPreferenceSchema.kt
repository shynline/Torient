package app.shynline.torient.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "torrent_preference")
data class TorrentPreferenceSchema(
    @PrimaryKey
    @ColumnInfo(name = "info_hash")
    var infoHash: String,
    @ColumnInfo(name = "honor_global_rate")
    var honorGlobalRate: Boolean = true,
    @ColumnInfo(name = "download_rate_limit")
    var downloadRateLimit: Boolean = false,
    @ColumnInfo(name = "download_rate")
    var downloadRate: Int = 100,
    @ColumnInfo(name = "upload_rate_limit")
    var uploadRateLimit: Boolean = false,
    @ColumnInfo(name = "upload_rate")
    var uploadRate: Int = 100
)