package app.shynline.torient.torrent.exts

private val HEX_ARRAY = "0123456789ABCDEF".toCharArray()
fun ByteArray.toHexString(): String {
    val hexChars = CharArray(this.size * 2)
    var byte: Int
    for (j in this.indices) {
        byte = this[j].toInt() and 0xFF
        hexChars[j * 2] = HEX_ARRAY[byte.ushr(4)]
        hexChars[j * 2 + 1] = HEX_ARRAY[byte and 0x0F]
    }

    return String(hexChars)
}
