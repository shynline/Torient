package app.shynline.torient.screens.common.requesthelper

interface FragmentRequestHelper {
    fun openTorrentFile(requestId: Int)
    fun saveToDownload(name: String)
    fun copyMagnetToClipBoard(name: String, magnet: String)
}