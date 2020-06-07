package app.shynline.torient.domain.database.common.typeconverter


object LongArrayConverter {
    fun toLongArray(str: String?): List<Long>? {
        str?.let { string ->
            return string.split(",").map { it.toLong() }
        }
        return null
    }

    fun toString(longArray: List<Long>?): String? {
        longArray?.let {
            return longArray.joinToString(",")
        }
        return null
    }
}