package dataset

import app.shynline.torient.domain.database.common.states.TorrentUserState
import app.shynline.torient.domain.database.entities.TorrentSchema
import app.shynline.torient.domain.models.TorrentModel
import java.util.*

object TorrentSchemaUtils {
    fun getSchema(): TorrentSchema {
        return TorrentSchema(
            "infohash",
            "magnet",
            TorrentUserState.ACTIVE,
            progress = 0f,
            name = "cool name",
            lastSeenComplete = Date().time
        ).apply {
            val random = Random()
            fileProgress = List(10) { random.nextLong() }
        }
    }

    fun getSchema(torrentModel: TorrentModel, userState: TorrentUserState): TorrentSchema {
        return TorrentSchema(
            torrentModel.infoHash,
            torrentModel.magnet,
            userState,
            name = torrentModel.name
        )
    }
}