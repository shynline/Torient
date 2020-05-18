package app.shynline.torient.model

enum class FilePriority(val id: Int) {
    NORMAL(0),
    HIGH(1),
    LOW(2),
    MIXED(3)
}

fun getPriority(id: Int): FilePriority {
    FilePriority.values().forEach {
        if (id == it.id)
            return it
    }
    throw NoSuchElementException("Invalid enum id.")
}

data class TorrentFilePriority(
    var active: Boolean,
    var priority: FilePriority
) {

    override fun toString(): String {
        return "${if (active) 1 else 0}|${priority.id}"
    }

    companion object {
        fun default() = TorrentFilePriority(true, FilePriority.NORMAL)
        fun fromString(str: String): TorrentFilePriority {
            val s = str.split("|")
            return TorrentFilePriority(
                active = s[0].toBoolean(),
                priority = getPriority(s[1].toInt())
            )
        }
    }
}