package app.shynline.torient.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.shynline.torient.database.entities.TorrentSchema
import app.shynline.torient.database.typeconverter.TorrentStateConverter

/**
 *
 * Note that exportSchema should be true in production databases.
 */
@Database(entities = [TorrentSchema::class], version = 1, exportSchema = false)
@TypeConverters(TorrentStateConverter::class)
abstract class TorrentDataBase : RoomDatabase() {
    abstract fun torrentDao(): TorrentDao
}