package dataset

import app.shynline.torient.torrent.bencoding.BDict
import app.shynline.torient.torrent.bencoding.BInteger
import app.shynline.torient.torrent.bencoding.BList
import app.shynline.torient.torrent.bencoding.BString
import app.shynline.torient.torrent.bencoding.common.BItem


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
            add(
                Pair(
                    "li4ei0ei-1ei50ei-80ei13ee", listOf(
                        BInteger(item = 4),
                        BInteger(item = 0),
                        BInteger(item = -1),
                        BInteger(item = 50),
                        BInteger(item = -80),
                        BInteger(item = 13)
                    )
                )
            )
            add(Pair("l4:testi-8ee", listOf(BString(item = "test"), BInteger(item = -8))))
            add(
                Pair(
                    "l7:torrentl5:inneri3eee",
                    listOf(
                        BString(item = "torrent"),
                        BList(item = listOf(BString(item = "inner"), BInteger(item = 3)))
                    )
                )
            )
            add(
                Pair(
                    "li4e2:god3:cow3:moo4:spam4:eggsee",
                    listOf(
                        BInteger(item = 4),
                        BString(item = "go"),
                        BDict(
                            item = linkedMapOf(
                                Pair(
                                    BString(item = "cow"), BString(item = "moo")
                                ),
                                Pair(
                                    BString(item = "spam"), BString(item = "eggs")
                                )
                            )
                        )
                    )
                )
            )
        }
    }
}

object BDictSamples {
    private val _data: MutableList<Pair<String, LinkedHashMap<BString, BItem<*>>>> = mutableListOf()
    val data: List<Pair<String, LinkedHashMap<BString, BItem<*>>>> = _data

    init {
        _data.apply {
            add(
                Pair(
                    "de",
                    linkedMapOf()
                )
            )
            add(
                Pair(
                    "d3:cow3:moo4:spam4:eggse",
                    linkedMapOf(
                        Pair(BString(item = "cow"), BString(item = "moo")),
                        Pair(BString(item = "spam"), BString(item = "eggs"))
                    )
                ) as Pair<String, LinkedHashMap<BString, BItem<*>>>
            )
            add(
                Pair(
                    "d4:spaml1:a1:bee",
                    linkedMapOf(
                        Pair(
                            BString(item = "spam"),
                            BList(item = listOf(BString(item = "a"), BString(item = "b")))
                        )
                    )
                ) as Pair<String, LinkedHashMap<BString, BItem<*>>>
            )
            add(
                Pair(
                    "d9:publisher3:bob17:publisher-webpage15:www.example.com18:publisher.location4:home2:soi4ee",
                    linkedMapOf(
                        Pair(BString(item = "publisher"), BString(item = "bob")),
                        Pair(
                            BString(item = "publisher-webpage"),
                            BString(item = "www.example.com")
                        ),
                        Pair(BString(item = "publisher.location"), BString(item = "home")),
                        Pair(BString(item = "so"), BInteger(item = 4))
                    )
                ) as Pair<String, LinkedHashMap<BString, BItem<*>>>
            )
            add(
                Pair(
                    "d2:soi4e4:dictdee",
                    linkedMapOf(
                        Pair(BString(item = "so"), BInteger(item = 4)),
                        Pair(BString(item = "dict"), BDict(item = linkedMapOf()))
                    )
                ) as Pair<String, LinkedHashMap<BString, BItem<*>>>
            )
            add(
                Pair(
                    "d2:so4:test4:dictd3:cow3:moo4:spam4:eggsee",
                    linkedMapOf(
                        Pair(BString(item = "so"), BString(item = "test")),
                        Pair(
                            BString(item = "dict"), BDict(
                                item = linkedMapOf(
                                    Pair(BString(item = "cow"), BString(item = "moo")),
                                    Pair(BString(item = "spam"), BString(item = "eggs"))
                                )
                            )
                        )
                    )
                ) as Pair<String, LinkedHashMap<BString, BItem<*>>>
            )
        }
    }
}