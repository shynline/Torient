package app.shynline.torient.model

import app.shynline.torient.database.common.states.TorrentUserState

data class TorrentOverview(
    var name: String, val infoHash: String, val size: Long, val numPiece: Int,
    val pieceLength: Int, var progress: Float, var userState: TorrentUserState,
    val creator: String, val comment: String, val createdDate: Long, val private: Boolean,
    var lastSeenComplete: Long
)