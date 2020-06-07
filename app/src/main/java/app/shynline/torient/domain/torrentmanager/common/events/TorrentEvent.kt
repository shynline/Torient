package app.shynline.torient.domain.torrentmanager.common.events

import app.shynline.torient.domain.models.TorrentModel
import app.shynline.torient.domain.torrentmanager.common.states.TorrentDownloadingState

sealed class TorrentEvent(val infoHash: String)
class TorrentProgressEvent(
    infoHash: String,
    val state: TorrentDownloadingState,
    val progress: Float = 0f,
    val downloadRate: Int = 0,
    val uploadRate: Int = 0,
    val maxPeers: Int = 0,
    val connectedPeers: Int = 0,
    val fileProgress: List<Long>? = null
) : TorrentEvent(infoHash) {
    companion object {
        fun checkingFileEvent(infoHash: String, progress: Float): TorrentProgressEvent {
            return TorrentProgressEvent(
                infoHash, TorrentDownloadingState.CHECKING_FILES, progress = progress
            )
        }

        fun downloadingMetaDataEvent(infoHash: String): TorrentProgressEvent {
            return TorrentProgressEvent(infoHash, TorrentDownloadingState.DOWNLOADING_METADATA)
        }

        fun downloadingEvent(
            infoHash: String, progress: Float, downloadRate: Int, uploadRate: Int,
            maxPeers: Int, connectedPeers: Int, fileProgress: List<Long>
        ): TorrentProgressEvent {
            return TorrentProgressEvent(
                infoHash, TorrentDownloadingState.DOWNLOADING,
                progress = progress, downloadRate = downloadRate, uploadRate = uploadRate,
                maxPeers = maxPeers, connectedPeers = connectedPeers, fileProgress = fileProgress
            )
        }

        fun finishedEvent(infoHash: String): TorrentProgressEvent {
            return TorrentProgressEvent(infoHash, TorrentDownloadingState.FINISHED)
        }

        fun seedingEvent(
            infoHash: String, downloadRate: Int, uploadRate: Int, maxPeers: Int, connectedPeers: Int
        ): TorrentProgressEvent {
            return TorrentProgressEvent(
                infoHash, TorrentDownloadingState.SEEDING, downloadRate = downloadRate,
                uploadRate = uploadRate, maxPeers = maxPeers, connectedPeers = connectedPeers
            )
        }

        fun allocatingEvent(infoHash: String): TorrentProgressEvent {
            return TorrentProgressEvent(infoHash, TorrentDownloadingState.ALLOCATING)
        }

        fun checkingResumeDateEvent(infoHash: String): TorrentProgressEvent {
            return TorrentProgressEvent(infoHash, TorrentDownloadingState.CHECKING_RESUME_DATA)
        }

        fun unknownEvent(infoHash: String): TorrentProgressEvent {
            return TorrentProgressEvent(infoHash, TorrentDownloadingState.UNKNOWN)
        }
    }
}

class TorrentMetaDataEvent(infoHash: String, val torrentModel: TorrentModel) :
    TorrentEvent(infoHash)