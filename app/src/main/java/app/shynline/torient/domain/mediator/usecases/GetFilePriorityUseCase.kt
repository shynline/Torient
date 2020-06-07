package app.shynline.torient.domain.mediator.usecases

import app.shynline.torient.domain.database.datasource.torrentfilepriority.TorrentFilePriorityDataSource
import app.shynline.torient.domain.models.TorrentFilePriority
import app.shynline.torient.domain.mediator.UseCase
import com.frostwire.jlibtorrent.TorrentInfo

class GetFilePriorityUseCase(
    private val torrentFilePriorityDataSource: TorrentFilePriorityDataSource
) : UseCase<GetFilePriorityUseCase.In, GetFilePriorityUseCase.Out>() {
    data class In(val torrentInfo: TorrentInfo)
    data class Out(val filePriority: List<TorrentFilePriority>?)

    override suspend fun execute(input: In): Out {
        if (input.torrentInfo.isValid) { // Torrent meta data is present
            val infoHash = input.torrentInfo.infoHash().toHex()
            val schema = torrentFilePriorityDataSource.getPriority(infoHash)
            if (schema.filePriority == null) {
                schema.defaultFilePriority(input.torrentInfo.numFiles())
                // Update database with generated priorities
                torrentFilePriorityDataSource.setPriority(schema)
            }
            return Out(schema.filePriority!!)
        }
        return Out(null)
    }
}