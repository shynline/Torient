package app.shynline.torient.torrent.bencoding.common

fun assertJustOneSet(
    method: String,
    b: Any?,
    a: String?
) {
    if (a == null && b == null) {
        throw IllegalArgumentException("$method: You must specify a String or an object")
    }
    if (a != null && b != null) {
        throw IllegalArgumentException("$method: You must specify either String or an object")
    }
}