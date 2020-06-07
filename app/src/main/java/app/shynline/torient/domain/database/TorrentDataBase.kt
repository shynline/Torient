package app.shynline.torient.domain.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.shynline.torient.domain.database.common.typeconverter.FilePriorityConverter
import app.shynline.torient.domain.database.common.typeconverter.TorrentStateConverter
import app.shynline.torient.domain.database.dao.TorrentDao
import app.shynline.torient.domain.database.dao.TorrentFilePriorityDao
import app.shynline.torient.domain.database.dao.TorrentPreferenceDao
import app.shynline.torient.domain.database.entities.TorrentFilePrioritySchema
import app.shynline.torient.domain.database.entities.TorrentPreferenceSchema
import app.shynline.torient.domain.database.entities.TorrentSchema

/**
 *
 * Note that exportSchema should be true in production databases.
 */
@Database(
    entities = [TorrentSchema::class, TorrentFilePrioritySchema::class, TorrentPreferenceSchema::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    TorrentStateConverter::class,
    FilePriorityConverter::class
)
abstract class TorrentDataBase : RoomDatabase() {
    abstract fun torrentDao(): TorrentDao
    abstract fun torrentFilePriorityDao(): TorrentFilePriorityDao
    abstract fun torrentPreferenceDao(): TorrentPreferenceDao
}