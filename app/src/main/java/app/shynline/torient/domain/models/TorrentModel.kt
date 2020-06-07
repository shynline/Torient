package app.shynline.torient.domain.models

import app.shynline.torient.domain.database.common.states.TorrentUserState
import app.shynline.torient.domain.torrentmanager.common.states.TorrentDownloadingState
import com.frostwire.jlibtorrent.TorrentInfo
import java.util.*

data class TorrentModel(
    val infoHash: String,
    var name: String,
    var magnet: String
) {
    var hexHash: Long = infoHash.hashCode().toLong()
    var author: String = ""
    var comment: String = ""
    var totalSize: Long = 0L
    var torrentFile: TorrentFile? = null
    var numFiles: Int = 0
    var numCompletedFiles: Int = 0
    var userState: TorrentUserState = TorrentUserState.PAUSED
    var downloadingState: TorrentDownloadingState = TorrentDownloadingState.UNKNOWN
    var progress = 0f
    var downloadRate = 0
    var uploadRate = 0
    var finished = false
    var maxPeers = 0
    var connectedPeers = 0
    var dateAdded = Date()
    var filePriority: List<TorrentFilePriority>? = null
    var filesSize: List<Long>? = null
    var filesPath: List<String>? = null
    var selectedFilesBytesDone: Float = 0f
    var selectedFilesSize: Long = 0L
    fun toIdentifier(): TorrentIdentifier {
        return TorrentIdentifier(
            infoHash,
            magnet
        )
    }

    companion object {
        fun from(torrentInfo: TorrentInfo): TorrentModel {
            val torrentFileData = TorrentFile.from(torrentInfo)
            return TorrentModel(
                torrentInfo.infoHash().toHex(),
                torrentInfo.name(),
                torrentInfo.makeMagnetUri()
            ).apply {
                comment = torrentInfo.comment()
                totalSize = torrentInfo.totalSize()
                torrentFile = torrentFileData.torrentFile
                filesSize = torrentFileData.filesSize
                filesPath = torrentFileData.filesPath
                numFiles = torrentInfo.numFiles()
                author = torrentInfo.creator()
            }
        }
    }
}