package app.shynline.torient.torrent.bencoding

import java.util.*

class BDict(bencoded: String? = null, item: LinkedHashMap<BString, BItem<*>>? = null) :
    BItem<LinkedHashMap<BString, BItem<*>>>(bencoded, item) {

    override fun encode(): String {
        return buildString {
            append("d")
            value().forEach {
                append(it.key.encode())
                append(it.value.encode())
            }
            append("e")
        }
    }

    override fun decode(bencoded: String): LinkedHashMap<BString, BItem<*>> {
        var bc = bencoded.toLowerCase(Locale.ROOT)
        if (bc.first() != 'd')
            throw InvalidBencodedString("BDict literals should start with d and end with e.")
        bc = bc.substring(IntRange(1, bc.length - 1))
        val res: LinkedHashMap<BString, BItem<*>> = linkedMapOf()
        var index: Int
        var sub: String
        var parts: List<String>
        var size: Int
        var key: BString
        while (bc.first() != 'e') {
            if (!bc.first().isDigit())
                throw InvalidBencodedString("Invalid Bencoded Dict.")
            parts = bc.split(":")
            size = parts[0].length + parts[0].toInt() + 1
            sub = bc.substring(IntRange(0, size - 1))
            key = BString(bencoded = sub)
            bc = bc.drop(size)
            when (bc.first()) {
                'i' -> {
                    index = bc.indexOfFirst { it == 'e' }
                    if (index == -1)
                        throw InvalidBencodedString("Invalid Bencoded Dict.")
                    sub = bc.substring(IntRange(0, index))
                    res[key] = BInteger(bencoded = sub)
                    bc = bc.drop(sub.length)
                }
                'l' -> {
                    val bl = BList(bencoded = bc)
                    res[key] = bl
                    bc = bc.drop(bl.encode().length)
                }
                'd' -> {
                    val bd = BDict(bencoded = bc)
                    res[key] = bd
                    bc = bc.drop(bd.encode().length)
                }
                else -> {
                    if (!bc.first().isDigit())
                        throw InvalidBencodedString("Invalid Bencoded Dict.")
                    parts = bc.split(":")
                    size = parts[0].length + parts[0].toInt() + 1
                    sub = bc.substring(IntRange(0, size - 1))
                    res[key] = BString(bencoded = sub)
                    bc = bc.drop(size)
                }
            }
        }
        return res
    }
}