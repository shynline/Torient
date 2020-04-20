package app.shynline.torient.torrent.internal.announce

class AnnounceException : Exception {
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

    companion object {
        private const val serialVersionUID: Long = -1
    }
}