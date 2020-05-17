package app.shynline.torient.database.typeconverter

import androidx.room.TypeConverter
import app.shynline.torient.model.TorrentFilePriority


class FilePriorityConverter {
    @TypeConverter
    fun toFilePriorityArray(filePriority: String?): Array<TorrentFilePriority>? {
        return filePriority?.split(",")?.map { TorrentFilePriority.fromString(it) }?.toTypedArray()
    }

    @TypeConverter
    fun toString(filePriority: Array<TorrentFilePriority>?): String? {
        return filePriority?.joinToString(",")
    }
}