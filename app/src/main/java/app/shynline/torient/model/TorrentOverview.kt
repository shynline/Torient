package app.shynline.torient.model

import app.shynline.torient.database.common.states.TorrentUserState
import app.shynline.torient.database.entities.TorrentSchema

data class TorrentOverview(
    var name: String, val infoHash: String, val size: Long, val numPiece: Int,
    val pieceLength: Int, var progress: Float, var userState: TorrentUserState,
    val creator: String, val comment: String, val createdDate: Long, val private: Boolean,
    var lastSeenComplete: Long

)

fun defaultTorrentOverView(
    infoHash: String,
    torrentOverview: TorrentOverview?,
    torrentSchema: TorrentSchema
): TorrentOverview? {
    var torrentOverview1 = torrentOverview
    torrentOverview1 = TorrentOverview(
        name = torrentSchema.name,
        infoHash = infoHash,
        progress = 0f,
        numPiece = 0,
        pieceLength = 0,
        size = 0,
        userState = torrentSchema.userState,
        creator = "",
        comment = "",
        createdDate = 0,
        private = false,
        lastSeenComplete = torrentSchema.lastSeenComplete
    )
    return torrentOverview1
}
