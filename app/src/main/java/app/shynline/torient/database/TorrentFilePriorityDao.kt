package app.shynline.torient.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.shynline.torient.database.entities.TorrentFilePrioritySchema
import app.shynline.torient.model.TorrentFilePriority

@Dao
interface TorrentFilePriorityDao {
    @Query("SELECT * from torrent_file_priority WHERE info_hash = :infoHash")
    suspend fun getTorrentFilePrioritySchema(infoHash: String): TorrentFilePrioritySchema?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTorrent(torrentFilePrioritySchema: TorrentFilePrioritySchema)

    @Query("UPDATE torrent_file_priority SET file_priority=:priorities WHERE info_hash=:infoHash")
    suspend fun setTorrentFilePriorities(
        infoHash: String,
        priorities: List<TorrentFilePriority>?
    ): Int

    @Query("DELETE from torrent_file_priority WHERE info_hash = :infoHash")
    suspend fun removeTorrentFilePriority(infoHash: String)
}