package app.shynline.torient.torrent.bencoding

import app.shynline.torient.torrent.bencoding.common.BItem
import app.shynline.torient.torrent.bencoding.common.Chars
import app.shynline.torient.torrent.bencoding.common.InvalidBencodedException
import java.net.URLEncoder

class BString(bencoded: ByteArray? = null, item: ByteArray? = null) :
    BItem<ByteArray>(bencoded, item) {

    constructor(item: String) :
            this(bencoded = null, item = item.toByteArray())

    override fun encode(): ByteArray {
        val prefix = value().size.toString() + ":"
        return prefix.toByteArray() + value()
    }

    override fun decode(bencoded: ByteArray): ByteArray {
        val index = bencoded.indexOfFirst {
            it == Chars.separator
        }
        try {
            val num = String(bencoded.asList().subList(0, index).toByteArray()).toInt()
            if (num == 0)
                return ByteArray(0)
            return bencoded.asList().subList(index + 1, index + 1 + num).toByteArray()
        } catch (e: Exception) {
            throw InvalidBencodedException(
                "There is no \":\" in this BString."
            )
        }
    }

    fun toPureString(): String {
        return String(value())
    }

    override fun toString(short: Boolean, n: Int): String {
        return URLEncoder.encode(String(value(), Charsets.ISO_8859_1), Charsets.ISO_8859_1.name())
    }

}