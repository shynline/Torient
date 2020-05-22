package dataset

import app.shynline.torient.database.entities.TorrentSchema
import app.shynline.torient.database.states.TorrentUserState
import java.util.*

object TorrentSchemaUtils {
    fun getSchema(): TorrentSchema {
        return TorrentSchema(
            "infohash",
            "magnet",
            TorrentUserState.ACTIVE,
            isFinished = false,
            progress = 0f,
            name = "cool name",
            lastSeenComplete = Date().time
        ).apply {
            val random = Random()
            fileProgress = List(10) { random.nextLong() }
        }
    }
}