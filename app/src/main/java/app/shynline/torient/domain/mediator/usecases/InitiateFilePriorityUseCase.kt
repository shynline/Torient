package app.shynline.torient.domain.mediator.usecases

import app.shynline.torient.domain.database.datasource.torrentfilepriority.TorrentFilePriorityDataSource
import app.shynline.torient.domain.models.TorrentFilePriority
import app.shynline.torient.domain.mediator.UseCase

class InitiateFilePriorityUseCase(
    private val torrentFilePriorityDataSource: TorrentFilePriorityDataSource
) : UseCase<InitiateFilePriorityUseCase.In, InitiateFilePriorityUseCase.Out>() {
    data class In(val infoHash: String, val numFile: Int)
    data class Out(val initiated: Boolean)

    override suspend fun execute(input: In): Out {
        val p = torrentFilePriorityDataSource.getPriority(input.infoHash)
        if (p.filePriority == null) {
            // Generate default priorities
            p.filePriority = MutableList(input.numFile) { TorrentFilePriority.default() }
            // Update database with generated priorities
            torrentFilePriorityDataSource.setPriority(p)
            return Out(
                true
            )
        }
        return Out(
            false
        )
    }
}