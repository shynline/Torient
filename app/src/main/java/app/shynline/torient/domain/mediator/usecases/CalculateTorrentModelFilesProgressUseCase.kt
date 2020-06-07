package app.shynline.torient.domain.mediator.usecases

import app.shynline.torient.domain.models.TorrentModel
import app.shynline.torient.domain.mediator.UseCase

class CalculateTorrentModelFilesProgressUseCase :
    UseCase<CalculateTorrentModelFilesProgressUseCase.In, CalculateTorrentModelFilesProgressUseCase.Out>() {
    data class In(val torrentModel: TorrentModel, val fileProgress: List<Long>?)
    data class Out(val succeed: Boolean)

    override suspend fun execute(input: In): Out {
        val torrentModel = input.torrentModel
        val filePriority = torrentModel.filePriority
        if (torrentModel.torrentFile == null || filePriority == null) { // Meta data is not available
            torrentModel.selectedFilesBytesDone = 0f
            torrentModel.selectedFilesSize = torrentModel.totalSize
            return Out(false)
        }
        var selectedBytesDone = 0f
        var selectedSize = 0L
        var numCompletedSize = 0
        filePriority.forEachIndexed { index, torrentFilePriority ->
            val fileSize = torrentModel.filesSize!![index]
            val progress = input.fileProgress?.get(index) ?: 0
            if (fileSize == progress) {
                numCompletedSize += 1
            }
            if (torrentFilePriority.active) {
                selectedSize += fileSize
                selectedBytesDone += progress
            }
        }
        // Updating the model
        torrentModel.selectedFilesSize = selectedSize
        torrentModel.selectedFilesBytesDone = selectedBytesDone
        torrentModel.numCompletedFiles = numCompletedSize
        return Out(true)
    }
}