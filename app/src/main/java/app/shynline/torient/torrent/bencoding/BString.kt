package app.shynline.torient.torrent.bencoding

import app.shynline.torient.torrent.bencoding.common.BItem
import app.shynline.torient.torrent.bencoding.common.InvalidBencodedString

class BString(bencoded: String? = null, item: String? = null) : BItem<String>(bencoded, item) {

    override fun encode(): String {
        return buildString {
            append(value().length)
            append(":")
            append(value())
        }
    }

    override fun decode(bencoded: String): String {
        if (bencoded.indexOfFirst { it == ':' } == -1)
            throw InvalidBencodedString(
                "There is no \":\" in this BString."
            )
        return bencoded.split(":")[1]
    }
}