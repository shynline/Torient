package app.shynline.torient.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.shynline.torient.database.entities.TorrentSchema
import app.shynline.torient.database.states.TorrentUserState
import kotlinx.coroutines.flow.Flow

@Dao
interface TorrentDao {

    @Query("SELECT * from torrent")
    fun getTorrents(): Flow<List<TorrentSchema>>

    @Query("SELECT * from torrent WHERE info_hash = :infoHash")
    suspend fun getTorrentByInfoHash(infoHash: String): TorrentSchema

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTorrent(torrentSchema: TorrentSchema)

    @Query("DELETE FROM torrent")
    suspend fun deleteAllTorrents()

    @Query("SELECT state from torrent WHERE info_hash= :infoHash")
    suspend fun getTorrentState(infoHash: String): TorrentUserState

    @Query("UPDATE torrent SET state = :userState WHERE info_hash = :infoHash")
    suspend fun setTorrentState(infoHash: String, userState: TorrentUserState)

    @Query("UPDATE torrent SET is_finished = :finished WHERE info_hash = :infoHash")
    suspend fun setTorrentFinished(infoHash: String, finished: Boolean)

    @Query("UPDATE torrent SET progress = :progress, last_seen_complete = :lastSeenComplete WHERE info_hash = :infoHash")
    suspend fun setTorrentProgress(
        infoHash: String,
        progress: Float,
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