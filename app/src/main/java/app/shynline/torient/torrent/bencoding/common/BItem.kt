package app.shynline.torient.torrent.bencoding.common

import app.shynline.torient.torrent.bencoding.BDict
import app.shynline.torient.torrent.bencoding.BString

abstract class BItem<TYPE>(bencoded: ByteArray?, item: TYPE?) {
    private var data: TYPE? = null

    init {
        assertJustOneSet(
            "initialize",
            item,
            bencoded
        )
        initialize(bencoded, item)
    }

    private fun initialize(bencoded: ByteArray?, item: TYPE?) {
        data = item ?: decode(bencoded!!)
    }

    fun value(): TYPE = data!!
    abstract fun encode(): ByteArray
    protected abstract fun decode(bencoded: ByteArray): TYPE
    abstract fun toString(short: Boolean = true, n: Int = 3): String

    override fun equals(other: Any?): Boolean {
        if (other is BItem<*>)
            return other.encode().contentEquals(encode())
        return false
    }

    override fun hashCode(): Int {
        return encode().toList().hashCode()
    }

    override fun toString(): String {
        return toString(true)
    }

}
