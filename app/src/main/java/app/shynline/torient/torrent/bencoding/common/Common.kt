package app.shynline.torient.torrent.bencoding.common

fun assertJustOneSet(
    method: String,
    b: Any?,
    a: ByteArray?
) {
    if (a == null && b == null) {
        throw IllegalArgumentException("$method: You must specify a String or an object")
    }
    if (a != null && b != null) {
        throw IllegalArgumentException("$method: You must specify either String or an object")
    }
}

object Chars {
    val separator = ':'.toByte()
    val i = 'i'.toByte()
    val e = 'e'.toByte()
    val l = 'l'.toByte()
    val d = 'd'.toByte()
}
