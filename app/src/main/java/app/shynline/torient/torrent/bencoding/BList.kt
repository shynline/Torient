package app.shynline.torient.torrent.bencoding

import app.shynline.torient.torrent.bencoding.common.BItem
import app.shynline.torient.torrent.bencoding.common.Chars
import app.shynline.torient.torrent.bencoding.common.InvalidBencodedException

class BList(bencoded: ByteArray? = null, item: List<BItem<*>>? = null) :
    BItem<List<BItem<*>>>(bencoded, item) {

    override fun encode(): ByteArray {
        var res = "l".toByteArray()
        value().forEach {
            res += it.encode()
        }
        res += "e".toByteArray()
        return res
    }

    override fun decode(bencoded: ByteArray): List<BItem<*>> {
        var bc = bencoded.copyOf()
        if (bc[0] != Chars.l)
            throw InvalidBencodedException(
                "BList literals should start with l and end with e."
            )
        bc = bc.copyOfRange(1, bc.size)
        val res: MutableList<BItem<*>> = mutableListOf()
        var index: Int
        var sub: ByteArray
        while (bc[0] != Chars.e) {
            when (bc[0]) {
                Chars.i -> {
                    index = bc.indexOfFirst { it == Chars.e }
                    if (index == -1)
                        throw InvalidBencodedException(
                            "Invalid Bencoded List."
                        )
                    sub = bc.toList().subList(0, index + 1).toByteArray()
                    res.add(BInteger(bencoded = sub))
                    bc = bc.copyOfRange(sub.size, bc.size)
                }
                Chars.l -> {
                    val bl = BList(bencoded = bc)
                    res.add(bl)
                    bc = bc.copyOfRange(bl.encode().size, bc.size)
                }
                Chars.d -> {
                    val bd = BDict(bencoded = bc)
                    res.add(bd)
                    bc = bc.copyOfRange(bd.encode().size, bc.size)
                }
                else -> {
                    if (!bc[0].toChar().isDigit())
                        throw InvalidBencodedException(
                            "Invalid Bencoded List."
                        )
                    val str = BString(bencoded = bc)
                    res.add(str)
                    bc = bc.copyOfRange(str.encode().size, bc.size)
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