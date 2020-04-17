package app.shynline.torient.torrent.bencoding

import app.shynline.torient.torrent.bencoding.common.BItem
import app.shynline.torient.torrent.bencoding.common.Chars
import app.shynline.torient.torrent.bencoding.common.InvalidBencodedException

class BInteger(bencoded: ByteArray? = null, item: Long? = null) : BItem<Long>(bencoded, item) {
    override fun encode(): ByteArray {
        return buildString {
            append("i")
            append(value())
            append("e")
        }.toByteArray()
    }

    override fun decode(bencoded: ByteArray): Long {
        if (bencoded[0] != Chars.i)
            throw InvalidBencodedException(
                "BInteger literals should start with i and end with e."
            )
        val index = bencoded.indexOfFirst {
            it == Chars.e
        }

        return String(bencoded.asList().subList(1, index).toByteArray()).toLong()
    }

    override fun toString(short: Boolean, n: Int): String {
        return value().toString()
    }

}