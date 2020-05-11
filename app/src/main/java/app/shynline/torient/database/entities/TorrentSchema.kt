package app.shynline.torient.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import app.shynline.torient.database.states.TorrentUserState
import app.shynline.torient.model.TorrentIdentifier

@Entity(tableName = "torrent")
data class TorrentSchema(
    @PrimaryKey
    @ColumnInfo(name = "info_hash")
    var infoHash: String,
    @ColumnInfo(name = "magnet")
    var magnet: String,
    @ColumnInfo(name = "state")
    var userState: TorrentUserState,
    @ColumnInfo(name = "is_finished")
    var isFinished: Boolean = false,
    @ColumnInfo(name = "progress")
    var progress: Float = 0f,
    @ColumnInfo(name = "name")
    var name: String
) {
    fun toIdentifier(): TorrentIdentifier {
        return TorrentIdentifier(
            infoHash,
            magnet
        )
    }
}