package app.shynline.torient.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.shynline.torient.database.entities.TorrentSchema
import kotlinx.coroutines.flow.Flow

@Dao
interface TorrentDao {

    @Query("SELECT * from torrent")
    fun getTorrents(): Flow<List<TorrentSchema>>

    @Query("SELECT * from torrent WHERE info_hash = :infoHash")
    fun getTorrentByInfoHash(infoHash: String): TorrentSchema

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTorrent(torrentSchema: TorrentSchema)

    @Query("DELETE FROM torrent")
    fun deleteAllTorrents()

    @Query("SELECT state from torrent WHERE info_hash= :infoHash")
    fun getTorrentState(infoHash: String): TorrentUserState

    @Query("UPDATE torrent SET state = :userState WHERE info_hash = :infoHash")
    fun setTorrentState(infoHash: String, userState: TorrentUserState)

    @Query("UPDATE torrent SET is_finished = :finished WHERE info_hash = :infoHash")
    fun setTorrentFinished(infoHash: String, finished: Boolean)

    @Query("UPDATE torrent SET progress = :progress WHERE info_hash = :infoHash")
    fun setTorrentProgress(infoHash: String, progress: Float)
}