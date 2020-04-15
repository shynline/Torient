package dataset

import app.shynline.torient.torrent.bencoding.BInteger
import app.shynline.torient.torrent.bencoding.BItem
import app.shynline.torient.torrent.bencoding.BString


object BIntegerSamples {
    private val _data: MutableList<Pair<String, Long>> = mutableListOf()
    val data: List<Pair<String, Long>> = _data

    init {
        _data.apply {
            add(Pair("i3e", 3))
            add(Pair("i-2500e", -2500))
            add(Pair("i0e", 0))
            add(Pair("i-18e", -18))
            add(Pair("i7000e", 7000))
        }
    }
}

object BStringSamples {
    private val _data: MutableList<Pair<String, String>> = mutableListOf()
    val data: List<Pair<String, String>> = _data

    init {
        _data.apply {
            add(Pair("0:", ""))
            add(Pair("4:spam", "spam"))
        }
    }
}

object BListSamples {
    private val _data: MutableList<Pair<String, List<BItem<*>>>> = mutableListOf()
    val data: List<Pair<String, List<BItem<*>>>> = _data

    init {
        _data.apply {
            add(Pair("le", listOf<BItem<Any>>()))
            add(Pair("l4:spam4:eggse", listOf(BString(item = "spam"), BString(item = "eggs"))))
            add(Pair("l0:e", listOf(BString(item = ""))))
            add(Pair("li4ee", listOf(BInteger(item = 4))))
            add(Pair("l4:testi-8ee", listOf(BString(item = "test"), BInteger(item = -8))))
        }
    }
}