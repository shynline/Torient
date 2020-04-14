package app.shynline.torient.torrent.bencoding

import java.util.*

class BInteger(bencoded: String? = null, item: Long? = null) : BItem<Long>(bencoded, item) {
    override fun encode(): String {
        return buildString {
            append("i")
            append(value())
            append("e")
        }
    }

    override fun decode(bencoded: String): Long {
        val bc = bencoded.toLowerCase(Locale.ROOT)
        if (bc.first() != 'i' || bc.last() != 'e')
            throw InvalidBencodedString("BInteger literals should start with i and end with e.")
        return bc.substring(IntRange(1, bc.length - 2)).toLong()
    }
}