package app.shynline.torient.database

import androidx.room.Database
import androidx.room.RoomDatabase
import app.shynline.torient.database.entities.TorrentSchema

/**
 *
 * Note that exportSchema should be true in production databases.
 */
@Database(entities = [TorrentSchema::class], version = 1, exportSchema = false)
abstract class TorrentDataBase : RoomDatabase() {
    abstract fun torrentDao(): TorrentDao
}