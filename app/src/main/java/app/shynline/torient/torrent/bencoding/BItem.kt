package app.shynline.torient.torrent.bencoding

abstract class BItem<TYPE>(bencoded: String?, item: TYPE?) {
    private var data: TYPE? = null

    init {
        assertJustOneSet("initialize", item, bencoded)
        initialize(bencoded, item)
    }

    private fun initialize(bencoded: String?, item: TYPE?) {
        data = item ?: decode(bencoded!!)
    }

    fun value(): TYPE = data!!
    abstract fun encode(): String
    protected abstract fun decode(bencoded: String): TYPE

    override fun equals(other: Any?): Boolean {
        if (other is BItem<*>)
            return other.value() == value()
        return false
    }
}
