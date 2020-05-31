package app.shynline.torient.torrent.mediator.usecases

import app.shynline.torient.database.common.states.TorrentUserState
import app.shynline.torient.database.datasource.torrent.TorrentDataSource
import app.shynline.torient.database.entities.TorrentSchema
import app.shynline.torient.torrent.mediator.UseCase

class AddTorrentToDataBaseUseCase(
    private val torrentDataSource: TorrentDataSource,
    private val initiateFilePriorityUseCase: InitiateFilePriorityUseCase
) : UseCase<AddTorrentToDataBaseUseCase.In, AddTorrentToDataBaseUseCase.Out>() {
    data class In(
        val infoHash: String,
        val name: String,
        val magnet: String,
        val state: TorrentUserState,
        val initiateFilePriorities: Boolean = false,
        val numFiles: Int = 0
    )

    data class Out(val created: Boolean)

    override suspend fun execute(input: In): Out {
        if (torrentDataSource.getTorrent(input.infoHash) == null) {
            torrentDataSource.insertTorrent(
                TorrentSchema(
                    infoHash = input.infoHash,
                    name = input.name,
                    magnet = input.magnet,
                    userState = input.state
                )
            )
            if (input.initiateFilePriorities) {
                initiateFilePriorityUseCase(
                    InitiateFilePriorityUseCase.In(
                        input.infoHash,
                        input.numFiles
                    )
                )
            }
            return Out(
                true
            )
        }
        return Out(
            false
        )
    }
}