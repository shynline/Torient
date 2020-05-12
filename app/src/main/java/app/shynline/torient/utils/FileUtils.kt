package app.shynline.torient.utils

import java.io.File

fun File.calculateTotalSize(): Long {
    if (this.isDirectory) {
        var s = 0L
        this.listFiles()?.forEach {
            s += it.calculateTotalSize()
        }
        return s
    }
    return this.length()
}