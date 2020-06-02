package app.shynline.torient.torrent.mediator.usecases

import app.shynline.torient.database.datasource.torrent.TorrentDataSource
import app.shynline.torient.database.entities.TorrentSchema
import app.shynline.torient.torrent.mediator.UseCase

class GetTorrentSchemeUseCase(
    private val torrentDataSource: TorrentDataSource
) : UseCase<GetTorrentSchemeUseCase.In, GetTorrentSchemeUseCase.Out>() {
    data class In(val infoHash: String)
    data class Out(val torrentScheme: TorrentSchema? = null)

    override suspend fun execute(input: In): Out {
        return Out(torrentDataSource.getTorrent(input.infoHash))
    }
}