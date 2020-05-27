package app.shynline.torient.utils

import app.shynline.torient.Config
import java.io.File
import java.io.InputStream
import java.io.OutputStream

fun File.calculateTotalCompletedSize(parent: String, completedFiles: List<String>): Long {
    val path = parent + (if (parent.isBlank()) "" else File.separator) + this.name
    if (this.isDirectory) {
        var s = 0L
        this.listFiles()?.forEach {
            s += it.calculateTotalCompletedSize(path, completedFiles)
        }
        return s
    }
    if (!completedFiles.contains(path))
        return 0
    return this.length()
}

fun InputStream.copyTo(
    out: OutputStream,
    bufferSize: Int = Config.DEFAULT_FILE_BUFFER_SIZE,
    checkPoint: Int = Config.DEFAULT_COPY_FILE_CHECKPOINT_SIZE,
    callback: (delta: Long, progress: Long) -> Unit
): Long {
    var bytesCopied: Long = 0
    val buffer = ByteArray(bufferSize)
    var currentCheckPoint = 0L
    var bytes = read(buffer)
    while (bytes >= 0) {
        out.write(buffer, 0, bytes)
        bytesCopied += bytes
        currentCheckPoint += bytes
        if (currentCheckPoint >= checkPoint) {
            callback.invoke(currentCheckPoint, bytesCopied)
            currentCheckPoint = 0
        }
        bytes = read(buffer)
    }
    if (currentCheckPoint > 0) {
        callback.invoke(currentCheckPoint, bytesCopied)
    }
    return bytesCopied
}