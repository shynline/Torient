package app.shynline.torient.torrent.bencoding

import app.shynline.torient.torrent.bencoding.common.BItem
import app.shynline.torient.torrent.bencoding.common.InvalidBencodedString
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
        if (bc.first() != 'l')
            throw InvalidBencodedString(
                "BList literals should start with l and end with e."
            )
        bc = bc.substring(IntRange(1, bc.length - 1))
        val res: MutableList<BItem<*>> = mutableListOf()
        var index: Int
        var sub: String
        var parts: List<String>
        var size: Int
        while (bc.first() != 'e') {
            when (bc.first()) {
                'i' -> {
                    index = bc.indexOfFirst { it == 'e' }
                    if (index == -1)
                        throw InvalidBencodedString(
                            "Invalid Bencoded List."
                        )
                    sub = bc.substring(IntRange(0, index))
                    res.add(BInteger(bencoded = sub))
                    bc = bc.drop(sub.length)
                }
                'l' -> {
                    val bl = BList(bencoded = bc)
                    res.add(bl)
                    bc = bc.drop(bl.encode().length)
                }
                'd' -> {
                    val bd = BDict(bencoded = bc)
                    res.add(bd)
                    bc = bc.drop(bd.encode().length)
                }
                else -> {
                    if (!bc.first().isDigit())
                        throw InvalidBencodedString(
                            "Invalid Bencoded List."
                        )
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

    override fun toString(short: Boolean, n: Int): String {
        var mShort = short
        val size = value().size
        if (size < n + 1)
            mShort = false
        return buildString {
            append("[")
            value().filterIndexed { index, bItem ->
                !mShort || index == size - 1 || index < n - 1
            }.forEachIndexed { index, bItem ->
                append(" ")
                append(bItem.toString(mShort))
                if (!mShort) {
                    if (index < size - 1)
                        append(",")
                } else {
                    if (index < n - 1)
                        append(",")
                    if (index == n - 2) {
                        append(" ... ")
                        append(",")
                    }
                }
            }
            append(" ]")
        }
    }

}