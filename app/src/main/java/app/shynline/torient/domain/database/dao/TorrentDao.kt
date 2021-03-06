package app.shynline.torient.domain.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.shynline.torient.domain.database.common.states.TorrentUserState
import app.shynline.torient.domain.database.entities.TorrentSchema
import kotlinx.coroutines.flow.Flow

@Dao
interface TorrentDao {

    @Query("SELECT * from torrent")
    fun getTorrents(): Flow<List<TorrentSchema>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTorrent(torrentSchema: TorrentSchema)

    @Query("DELETE FROM torrent")
    suspend fun deleteAllTorrents()

    @Query("SELECT state from torrent WHERE info_hash= :infoHash")
    suspend fun getTorrentState(infoHash: String): TorrentUserState

    @Query("UPDATE torrent SET state = :userState WHERE info_hash = :infoHash")
    suspend fun setTorrentState(infoHash: String, userState: TorrentUserState)

    @Query("UPDATE torrent SET progress = :progress WHERE info_hash = :infoHash")
    suspend fun setTorrentProgress(
        infoHash: String,
        progress: Float
    )

    @Query("UPDATE torrent SET last_seen_complete = :lastSeenComplete WHERE info_hash = :infoHash")
    suspend fun setTorrentLastSeenComplete(
        infoHash: String,
        lastSeenComplete: Long
    )

    @Query("UPDATE torrent SET file_progress = :fileProgress WHERE info_hash = :infoHash")
    suspend fun setTorrentFileProgress(
        infoHash: String,
        fileProgress: String
    )

    @Query("SELECT * from torrent WHERE info_hash = :infoHash")
    suspend fun getTorrent(infoHash: String): TorrentSchema?

    @Query("DELETE from torrent WHERE info_hash = :infoHash")
    suspend fun removeTorrent(infoHash: String)
}