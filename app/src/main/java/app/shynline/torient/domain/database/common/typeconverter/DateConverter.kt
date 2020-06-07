package app.shynline.torient.domain.database.common.typeconverter

import androidx.room.TypeConverter
import java.util.*

class DateConverter {
    @TypeConverter
    fun toDate(date: Long): Date {
        return Date(date)
    }

    @TypeConverter
    fun toLong(date: Date): Long {
        return date.time
    }
}