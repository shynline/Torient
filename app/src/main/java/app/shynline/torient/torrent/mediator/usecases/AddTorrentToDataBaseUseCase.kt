package app.shynline.torient.torrent.mediator.usecases

import app.shynline.torient.database.common.states.TorrentUserState
import app.shynline.torient.database.datasource.torrent.TorrentDataSource
import app.shynline.torient.database.entities.TorrentSchema
import app.shynline.torient.model.TorrentModel
import app.shynline.torient.torrent.mediator.UseCase

class AddTorrentToDataBaseUseCase(
    private val torrentDataSource: TorrentDataSource,
    private val initiateFilePriorityUseCase: InitiateFilePriorityUseCase
) : UseCase<AddTorrentToDataBaseUseCase.In, AddTorrentToDataBaseUseCase.Out>() {
    data class In(val torrentModel: TorrentModel, val state: TorrentUserState)
    data class Out(val created: Boolean)

    override suspend fun execute(input: In): Out {
        if (torrentDataSource.getTorrent(input.torrentModel.infoHash) == null) {
            torrentDataSource.insertTorrent(
                TorrentSchema(
                    infoHash = input.torrentModel.infoHash,
                    name = input.torrentModel.name,
                    magnet = input.torrentModel.magnet,
                    userState = input.state
                )
            )
            initiateFilePriorityUseCase(
                InitiateFilePriorityUseCase.In(
                    input.torrentModel.infoHash,
                    input.torrentModel.numFiles
                )
            )
            return Out(
                true
            )
        }
        return Out(
            false
        )
    }
}