package app.shynline.torient.domain.mediator.usecases

import app.shynline.torient.domain.models.TorrentIdentifier
import app.shynline.torient.domain.models.TorrentModel
import app.shynline.torient.domain.mediator.UseCase
import app.shynline.torient.domain.torrentmanager.torrent.Torrent

class GetTorrentModelUseCase(
    private val torrent: Torrent
) : UseCase<GetTorrentModelUseCase.In, GetTorrentModelUseCase.Out>() {
    data class In(
        val infoHash: String? = null,
        val identifier: TorrentIdentifier? = null,
        val torrentFile: ByteArray? = null,
        val magnet: String? = null
    )

    data class Out(
        val torrentModel: TorrentModel?
    )

    override suspend fun execute(input: In): Out {
        input.infoHash?.let {
            return Out(
                torrent.getTorrentModelFromInfoHash(it)
            )
        }
        input.torrentFile?.let {
            return Out(
                torrent.getTorrentModel(it)
            )
        }
        input.identifier?.let {
            return Out(
                torrent.getTorrentModel(it)
            )
        }
        return Out(
            torrent.getTorrentModel(input.magnet!!)
        )
    }
}