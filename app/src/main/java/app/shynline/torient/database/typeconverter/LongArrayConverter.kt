package app.shynline.torient.database.typeconverter

import androidx.room.TypeConverter


class LongArrayConverter {
    @TypeConverter
    fun toLongArray(str: String?): LongArray? {
        str?.let { string ->
            return string.split(",").map { it.toLong() }.toLongArray()
        }
        return null
    }

    @TypeConverter
    fun toString(longArray: LongArray?): String? {
        longArray?.let {
            return longArray.joinToString(",")
        }
        return null
    }
}