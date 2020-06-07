package app.shynline.torient.domain.database.common.typeconverter

import androidx.room.TypeConverter
import app.shynline.torient.domain.models.TorrentFilePriority


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