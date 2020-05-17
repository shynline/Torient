package app.shynline.torient.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import app.shynline.torient.model.TorrentFilePriority

@Entity(tableName = "torrent_file_priority")
data class TorrentFilePrioritySchema(
    @PrimaryKey
    @ColumnInfo(name = "info_hash")
    var infoHash: String,
    @ColumnInfo(name = "file_priority")
    var filePriority: Array<TorrentFilePriority>? = null
)