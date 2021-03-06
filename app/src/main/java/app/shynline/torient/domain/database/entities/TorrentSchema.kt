package app.shynline.torient.domain.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import app.shynline.torient.domain.database.common.states.TorrentUserState
import app.shynline.torient.domain.database.common.typeconverter.LongArrayConverter
import app.shynline.torient.domain.models.TorrentIdentifier
import java.util.*

@Entity(tableName = "torrent")
data class TorrentSchema(
    @PrimaryKey
    @ColumnInfo(name = "info_hash")
    var infoHash: String,
    @ColumnInfo(name = "magnet")
    var magnet: String,
    @ColumnInfo(name = "state")
    var userState: TorrentUserState,
    @ColumnInfo(name = "progress")
    var progress: Float = 0f,
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "last_seen_complete")
    var lastSeenComplete: Long = 0L,
    @ColumnInfo(name = "file_progress")
    var _fileProgress: String? = null,
    @ColumnInfo(name = "date_added")
    var dateAdded: Date = Date()
) {

    val isFinished: Boolean
        get() {
            return 100f == progress
        }

    var fileProgress: List<Long>?
        set(value) {
            _fileProgress = LongArrayConverter.toString(value)
        }
        get() {
            return LongArrayConverter.toLongArray(_fileProgress)
        }
    fun toIdentifier(): TorrentIdentifier {
        return TorrentIdentifier(
            infoHash,
            magnet
        )
    }
}