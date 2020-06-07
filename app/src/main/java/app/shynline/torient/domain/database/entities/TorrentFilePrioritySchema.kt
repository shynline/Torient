package app.shynline.torient.domain.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import app.shynline.torient.domain.models.TorrentFilePriority

@Entity(tableName = "torrent_file_priority")
data class TorrentFilePrioritySchema(
    @PrimaryKey
    @ColumnInfo(name = "info_hash")
    var infoHash: String,
    @ColumnInfo(name = "file_priority")
    var filePriority: List<TorrentFilePriority>? = null
) {
    fun defaultFilePriority(numFile: Int) {
        // Generate default priorities
        filePriority = MutableList(
            numFile
        ) { TorrentFilePriority.default() }
    }
}