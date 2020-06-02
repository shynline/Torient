package app.shynline.torient.torrent.mediator.usecases

import app.shynline.torient.database.datasource.torrentfilepriority.TorrentFilePriorityDataSource
import app.shynline.torient.database.entities.TorrentFilePrioritySchema
import app.shynline.torient.torrent.mediator.UseCase

class GetTorrentFilePriorityUseCase(
    private val torrentFilePriorityDataSource: TorrentFilePriorityDataSource
) : UseCase<GetTorrentFilePriorityUseCase.In, GetTorrentFilePriorityUseCase.Out>() {
    data class In(val infoHash: String)
    data class Out(val torrentPriority: TorrentFilePrioritySchema)

    override suspend fun execute(input: In): Out {
        return Out(torrentFilePriorityDataSource.getPriority(input.infoHash))
    }
}