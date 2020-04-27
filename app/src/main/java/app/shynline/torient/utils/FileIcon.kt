package app.shynline.torient.utils

import app.shynline.torient.R
import app.shynline.torient.utils.FileType.*

object FileIcon {
    fun iconOf(fileType: FileType): Int {
        return when (fileType) {
            UNKNOWN -> R.drawable.icon_file
            APPLICATION -> R.drawable.icon_application
            ARCHIVE -> R.drawable.icon_archive
            AUDIO -> R.drawable.icon_audio
            DATABASE -> R.drawable.icon_database
            DISK_IMAGE -> R.drawable.icon_disk
            DOCUMENT -> R.drawable.icon_document
            FEED -> R.drawable.icon_rss
            FONT -> R.drawable.icon_font
            IMAGE -> R.drawable.icon_image
            PRESENTATION -> R.drawable.icon_presentation
            SPREADSHEET -> R.drawable.icon_spreadsheet
            VIDEO -> R.drawable.icon_video
            DIR -> R.drawable.icon_folder
        }
    }
}