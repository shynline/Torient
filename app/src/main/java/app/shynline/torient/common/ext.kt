package app.shynline.torient.common

import android.content.Context
import app.shynline.torient.Config
import java.io.File

val Context.downloadDir: File
    get() {
        val file = File(filesDir, Config.baseDownloadDir)
        if (!file.exists())
            file.mkdir()
        return file
    }

val Context.torrentDir: File
    get() {
        val file = File(filesDir, Config.baseTorrentDir)
        if (!file.exists())
            file.mkdir()
        return file
    }