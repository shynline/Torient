package app.shynline.torient.domain.mediator.usecases

import app.shynline.torient.domain.database.datasource.torrentfilepriority.TorrentFilePriorityDataSource
import app.shynline.torient.domain.database.entities.TorrentFilePrioritySchema
import app.shynline.torient.domain.mediator.UseCase
import app.shynline.torient.domain.torrentmanager.torrent.Torrent

class UpdateTorrentFilePriorityUseCase(
    private val torrent: Torrent,
    private val torrentFilePriorityDataSource: TorrentFilePriorityDataSource
) :
    UseCase<UpdateTorrentFilePriorityUseCase.In, UpdateTorrentFilePriorityUseCase.Out>() {
    data class In(
        val infoHash: String,
        val index: Int,
        val torrentFilePriority: TorrentFilePrioritySchema
    )

    object Out

    override suspend fun execute(input: In): Out {
        torrentFilePriorityDataSource.setPriority(input.torrentFilePriority)
        torrent.setFilePriority(
            input.infoHash,
            input.index,
            input.torrentFilePriority.filePriority!![input.index]
        )
        return Out
    }
}