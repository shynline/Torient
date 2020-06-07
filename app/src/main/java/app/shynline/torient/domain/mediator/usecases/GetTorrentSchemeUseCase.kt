package app.shynline.torient.domain.mediator.usecases

import app.shynline.torient.domain.database.datasource.torrent.TorrentDataSource
import app.shynline.torient.domain.database.entities.TorrentSchema
import app.shynline.torient.domain.mediator.UseCase

class GetTorrentSchemeUseCase(
    private val torrentDataSource: TorrentDataSource
) : UseCase<GetTorrentSchemeUseCase.In, GetTorrentSchemeUseCase.Out>() {
    data class In(val infoHash: String)
    data class Out(val torrentScheme: TorrentSchema? = null)

    override suspend fun execute(input: In): Out {
        return Out(torrentDataSource.getTorrent(input.infoHash))
    }
}