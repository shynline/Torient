package dataset

import app.shynline.torient.database.entities.TorrentFilePrioritySchema
import app.shynline.torient.model.FilePriority
import app.shynline.torient.model.TorrentFilePriority
import java.util.*

object TorrentFilePrioritySchemaUtils {
    fun getFilePrioritySchema(): TorrentFilePrioritySchema {
        return TorrentFilePrioritySchema(
            infoHash = "infohash",
            filePriority = getARandomFilePriorityList()
        )
    }

    fun getFilePrioritySchema(infoHash: String, numFile: Int): TorrentFilePrioritySchema {
        return TorrentFilePrioritySchema(
            infoHash = infoHash,
            filePriority = getARandomFilePriorityList(numFile)
        )
    }

    fun getFilePrioritySchemaNullFilePriority(infoHash: String): TorrentFilePrioritySchema {
        return TorrentFilePrioritySchema(
            infoHash = infoHash,
            filePriority = null
        )
    }

    fun getARandomFilePriorityList(numFile: Int): List<TorrentFilePriority> {
        val values = FilePriority.values()
        val random = Random()
        return List(numFile) {
            TorrentFilePriority(
                active = random.nextBoolean(),
                priority = values[random.nextInt(values.size)]
            )
        }
    }

    fun getARandomFilePriorityList(): List<TorrentFilePriority> {
        val values = FilePriority.values()
        val random = Random()
        return List(10) {
            TorrentFilePriority(
                active = random.nextBoolean(),
                priority = values[random.nextInt(values.size)]
            )
        }
    }
}