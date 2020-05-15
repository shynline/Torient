package app.shynline.torient.model

import app.shynline.torient.database.states.TorrentUserState

data class TorrentOverview(
    val name: String, val infoHash: String, val size: Long, val numPiece: Int,
    val pieceLength: Int, var progress: Float, var userState: TorrentUserState
)