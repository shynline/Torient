package app.shynline.torient.database.typeconverter

import androidx.room.TypeConverter
import app.shynline.torient.model.TorrentFilePriority


class FilePriorityConverter {
    @TypeConverter
    fun toFilePriorityArray(filePriority: String?): List<TorrentFilePriority>? {
        return filePriority?.split(",")?.map { TorrentFilePriority.fromString(it) }
    }

    @TypeConverter
    fun toString(filePriority: List<TorrentFilePriority>?): String? {
        return filePriority?.joinToString(",")
    }
}