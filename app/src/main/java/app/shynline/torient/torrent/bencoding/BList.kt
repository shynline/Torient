package app.shynline.torient.torrent.bencoding

import java.util.*

class BList(bencoded: String? = null, item: List<BItem<*>>? = null) :
    BItem<List<BItem<*>>>(bencoded, item) {

    override fun encode(): String {
        return buildString {
            append("l")
            value().forEach {
                append(it.encode())
            }
            append("e")
        }
    }

    override fun decode(bencoded: String): List<BItem<*>> {
        var bc = bencoded.toLowerCase(Locale.ROOT)
        if (bc.first() != 'l' || bc.last() != 'e')
            throw InvalidBencodedString("BList literals should start with l and end with e.")
        bc = bc.substring(IntRange(1, bc.length - 2))
        val res: MutableList<BItem<*>> = mutableListOf()
        var index: Int
        var sub: String
        var parts: List<String>
        var size: Int
        while (bc.isNotBlank()) {
            when (bc.first()) {
                'i' -> {
                    index = bc.indexOfFirst { it == 'e' }
                    if (index == -1)
                        throw InvalidBencodedString("Invalid Bencoded List.")
                    sub = bc.substring(IntRange(0, index))
                    res.add(BInteger(bencoded = sub))
                    bc = bc.drop(sub.length)
                }
                'l' -> {

                }
                'd' -> {

                }
                else -> {
                    if (!bc.first().isDigit())
                        throw InvalidBencodedString("Invalid Bencoded List.")
                    parts = bc.split(":")
                    size = parts[0].length + parts[0].toInt() + 1
                    sub = bc.substring(IntRange(0, size - 1))
                    res.add(BString(bencoded = sub))
                    bc = bc.drop(size)
                }
            }
        }
        return res
    }
}