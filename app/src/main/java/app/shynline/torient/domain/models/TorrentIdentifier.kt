package app.shynline.torient.domain.models

data class TorrentIdentifier(
    val infoHash: String,
    val magnet: String
)