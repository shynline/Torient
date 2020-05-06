package app.shynline.torient.utils

import java.util.*

enum class FileType {
    UNKNOWN,
    APPLICATION,
    ARCHIVE,
    AUDIO,
    DATABASE,
    DISK_IMAGE,
    DOCUMENT,
    FEED,
    FONT,
    IMAGE,
    PRESENTATION,
    SPREADSHEET,
    VIDEO,
    DIR
}

object FileTypeDetector {
    private val application = listOf("apk", "com", "exe", "xap")
    private val archive = listOf(
        "7z", "arc", "arj", "bzip2", "cab", "dar"
        , "gzip", "jar", "lzma2", "rar", "tar", "zip"
    )
    private val audio = listOf(
        "aac", "amr", "flac", "m3u", "midi", "mp3"
        , "ogg", "wav", "wma"
    )
    private val database = listOf("accdb", "mdb", "odb", "sqlite")
    private val diskImage = listOf("iso", "nrg", "vhd")
    private val document = listOf(
        "doc", "docx", "html", "json", "markdown", "odt"
        , "pdf", "rtf", "txt", "xml", "yaml"
    )
    private val feed = listOf("atom", "rss")
    private val font = listOf("otf", "ttf")
    private val image = listOf(
        "bmp", "gif", "ico", "jpeg"
        , "png", "psd", "tiff", "jpg"
    )
    private val presentation = listOf("odp", "ppt", "pptx")
    private val spreadsheet = listOf(
        "csv", "ods", "tsv"
        , "xls", "xlsx"
    )
    private val video = listOf(
        "3gp", "asf", "avi", "flv", "m4v", "mkv"
        , "mov", "mp4", "mpeg", "swf", "vob", "webm"
        , "wmv"
    )


    fun getType(name: String): FileType {
        if (name.isBlank())
            return FileType.UNKNOWN
        if (!name.contains('.'))
            return FileType.UNKNOWN
        val ext = name.split('.').last().toLowerCase(Locale.ROOT)
        if (ext.isBlank())
            return FileType.UNKNOWN
        if (application.contains(ext))
            return FileType.APPLICATION
        if (archive.contains(ext))
            return FileType.ARCHIVE
        if (audio.contains(ext))
            return FileType.AUDIO
        if (database.contains(ext))
            return FileType.DATABASE
        if (diskImage.contains(ext))
            return FileType.DISK_IMAGE
        if (document.contains(ext))
            return FileType.DOCUMENT
        if (feed.contains(ext))
            return FileType.FEED
        if (font.contains(ext))
            return FileType.FONT
        if (image.contains(ext))
            return FileType.IMAGE
        if (presentation.contains(ext))
            return FileType.PRESENTATION
        if (spreadsheet.contains(ext))
            return FileType.SPREADSHEET
        if (video.contains(ext))
            return FileType.VIDEO
        return FileType.UNKNOWN
    }
}

