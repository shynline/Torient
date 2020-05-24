package app.shynline.torient.database.dao

import androidx.room.*
import app.shynline.torient.database.entities.TorrentPreferenceSchema

@Dao
interface TorrentPreferenceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreference(preference: TorrentPreferenceSchema)

    @Query("SELECT * FROM torrent_preference WHERE info_hash=:infoHash")
    suspend fun getPreference(infoHash: String): TorrentPreferenceSchema?

    @Update
    suspend fun updateSchema(preference: TorrentPreferenceSchema): Int
}