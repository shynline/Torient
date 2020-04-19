package app.shynline.torient.torrent.bencoding

import app.shynline.torient.torrent.bencoding.common.BItem
import app.shynline.torient.torrent.bencoding.common.Chars
import app.shynline.torient.torrent.bencoding.common.InvalidBencodedException
import java.io.BufferedInputStream
import java.io.InputStream
import java.util.*

class BDict(bencoded: ByteArray? = null, item: LinkedHashMap<BString, BItem<*>>? = null) :
    BItem<LinkedHashMap<BString, BItem<*>>>(bencoded, item) {

    override fun encode(): ByteArray {
        var res = "d".toByteArray()
        value().forEach {
            res += it.key.encode()
            res += it.value.encode()
        }
        res += "e".toByteArray()
        return res
    }

    override fun decode(bencoded: ByteArray): LinkedHashMap<BString, BItem<*>> {
        var bc = bencoded.copyOf()
        if (bc[0] != Chars.d)
            throw InvalidBencodedException(
                "BDict literals should start with d and end with e."
            )
        bc = bc.copyOfRange(1, bc.size)
        val res: LinkedHashMap<BString, BItem<*>> = linkedMapOf()
        var index: Int
        var sub: ByteArray
        var key: BString
        var str: BString
        while (bc[0] != Chars.e) {
            if (!bc[0].toChar().isDigit())
                throw InvalidBencodedException(
                    "Invalid Bencoded Dict."
                )
            key = BString(bencoded = bc)
            bc = bc.copyOfRange(key.encode().size, bc.size)
            when (bc[0]) {
                Chars.i -> {
                    index = bc.indexOfFirst { it == Chars.e }
                    if (index == -1)
                        throw InvalidBencodedException(
                            "Invalid Bencoded Dict."
                        )
                    sub = bc.toList().subList(0, index + 1).toByteArray()
                    res[key] = BInteger(bencoded = sub)
                    bc = bc.copyOfRange(sub.size, bc.size)
                }
                Chars.l -> {
                    val bl = BList(bencoded = bc)
                    res[key] = bl
                    bc = bc.copyOfRange(bl.encode().size, bc.size)
                }
                Chars.d -> {
                    val bd = BDict(bencoded = bc)
                    res[key] = bd
                    bc = bc.copyOfRange(bd.encode().size, bc.size)
                }
                else -> {
                    if (!bc[0].toChar().isDigit())
                        throw InvalidBencodedException(
                            "Invalid Bencoded Dict."
                        )
                    str = BString(bencoded = bc)
                    res[key] = str
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
            append("{")
            value().toList().filterIndexed { index, pair ->
                !mShort || index == size - 1 || index < n - 1
            }.forEachIndexed { index, bItem ->
                append(" ")
                append(bItem.first.toString(mShort))
                append(":")
                append(bItem.second.toString(mShort))
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
            append(" }")
        }
    }

    operator fun get(key: String): BItem<*>? {
        return value()[BString(key)]
    }

    fun containsKey(key: String): Boolean {
        return value().containsKey(BString(key))
    }

    companion object {
        fun fromInputStream(inputStream: InputStream): BDict {
            return BDict(bencoded = BufferedInputStream(inputStream).use { s ->
                s.readBytes()
            })
        }
    }
}