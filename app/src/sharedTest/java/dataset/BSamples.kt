package dataset

import app.shynline.torient.torrent.bencoding.BDict
import app.shynline.torient.torrent.bencoding.BInteger
import app.shynline.torient.torrent.bencoding.BList
import app.shynline.torient.torrent.bencoding.BString
import app.shynline.torient.torrent.bencoding.common.BItem


object BIntegerSamples {
    private val _data: MutableList<Pair<ByteArray, Long>> = mutableListOf()
    val data: List<Pair<ByteArray, Long>> = _data

    init {
        _data.apply {
            add(Pair("i3e".toByteArray(), 3))
            add(Pair("i-2500e".toByteArray(), -2500))
            add(Pair("i0e".toByteArray(), 0))
            add(Pair("i-18e".toByteArray(), -18))
            add(Pair("i7000e".toByteArray(), 7000))
        }
    }
}

object BStringSamples {
    private val _data: MutableList<Pair<ByteArray, ByteArray>> = mutableListOf()
    val data: List<Pair<ByteArray, ByteArray>> = _data

    init {
        _data.apply {
            add(Pair("0:".toByteArray(), "".toByteArray()))
            add(Pair("4:spam".toByteArray(), "spam".toByteArray()))
        }
    }
}

object BListSamples {
    private val _data: MutableList<Pair<ByteArray, List<BItem<*>>>> = mutableListOf()
    val data: List<Pair<ByteArray, List<BItem<*>>>> = _data

    init {
        _data.apply {
            add(Pair("le".toByteArray(), listOf<BItem<Any>>()))
            add(
                Pair(
                    "l4:spam4:eggse".toByteArray(),
                    listOf(
                        BString(item = "spam".toByteArray()),
                        BString(item = "eggs".toByteArray())
                    )
                )
            )
            add(Pair("l0:e".toByteArray(), listOf(BString(item = "".toByteArray()))))
            add(Pair("li4ee".toByteArray(), listOf(BInteger(item = 4))))
            add(
                Pair(
                    "li4ei0ei-1ei50ei-80ei13ee".toByteArray(), listOf(
                        BInteger(item = 4),
                        BInteger(item = 0),
                        BInteger(item = -1),
                        BInteger(item = 50),
                        BInteger(item = -80),
                        BInteger(item = 13)
                    )
                )
            )
            add(
                Pair(
                    "l4:testi-8ee".toByteArray(),
                    listOf(BString(item = "test".toByteArray()), BInteger(item = -8))
                )
            )
            add(
                Pair(
                    "l7:torrentl5:inneri3eee".toByteArray(),
                    listOf(
                        BString(item = "torrent".toByteArray()),
                        BList(
                            item = listOf(
                                BString(item = "inner".toByteArray()),
                                BInteger(item = 3)
                            )
                        )
                    )
                )
            )
            add(
                Pair(
                    "li4e2:god3:cow3:moo4:spam4:eggsee".toByteArray(),
                    listOf(
                        BInteger(item = 4),
                        BString(item = "go".toByteArray()),
                        BDict(
                            item = linkedMapOf(
                                Pair(
                                    BString(item = "cow".toByteArray()),
                                    BString(item = "moo".toByteArray())
                                ),
                                Pair(
                                    BString(item = "spam".toByteArray()),
                                    BString(item = "eggs".toByteArray())
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
    private val _data: MutableList<Pair<ByteArray, LinkedHashMap<BString, BItem<*>>>> =
        mutableListOf()
    val data: List<Pair<ByteArray, LinkedHashMap<BString, BItem<*>>>> = _data

    init {
        _data.apply {
            add(
                Pair(
                    "de".toByteArray(),
                    linkedMapOf()
                )
            )
            add(
                Pair(
                    "d3:cow3:moo4:spam4:eggse".toByteArray(),
                    linkedMapOf(
                        Pair(
                            BString(item = "cow".toByteArray()),
                            BString(item = "moo".toByteArray())
                        ),
                        Pair(
                            BString(item = "spam".toByteArray()),
                            BString(item = "eggs".toByteArray())
                        )
                    )
                ) as Pair<ByteArray, LinkedHashMap<BString, BItem<*>>>
            )
            add(
                Pair(
                    "d4:spaml1:a1:bee".toByteArray(),
                    linkedMapOf(
                        Pair(
                            BString(item = "spam".toByteArray()),
                            BList(
                                item = listOf(
                                    BString(item = "a".toByteArray()),
                                    BString(item = "b".toByteArray())
                                )
                            )
                        )
                    )
                ) as Pair<ByteArray, LinkedHashMap<BString, BItem<*>>>
            )
            add(
                Pair(
                    "d9:publisher3:bob17:publisher-webpage15:www.example.com18:publisher.location4:home2:soi4ee".toByteArray(),
                    linkedMapOf(
                        Pair(
                            BString(item = "publisher".toByteArray()),
                            BString(item = "bob".toByteArray())
                        ),
                        Pair(
                            BString(item = "publisher-webpage".toByteArray()),
                            BString(item = "www.example.com".toByteArray())
                        ),
                        Pair(
                            BString(item = "publisher.location".toByteArray()),
                            BString(item = "home".toByteArray())
                        ),
                        Pair(BString(item = "so".toByteArray()), BInteger(item = 4))
                    )
                ) as Pair<ByteArray, LinkedHashMap<BString, BItem<*>>>
            )
            add(
                Pair(
                    "d2:soi4e4:dictdee".toByteArray(),
                    linkedMapOf(
                        Pair(BString(item = "so".toByteArray()), BInteger(item = 4)),
                        Pair(BString(item = "dict".toByteArray()), BDict(item = linkedMapOf()))
                    )
                ) as Pair<ByteArray, LinkedHashMap<BString, BItem<*>>>
            )
            add(
                Pair(
                    "d2:so4:test4:dictd3:cow3:moo4:spam4:eggsee".toByteArray(),
                    linkedMapOf(
                        Pair(
                            BString(item = "so".toByteArray()),
                            BString(item = "test".toByteArray())
                        ),
                        Pair(
                            BString(item = "dict".toByteArray()), BDict(
                                item = linkedMapOf(
                                    Pair(
                                        BString(item = "cow".toByteArray()),
                                        BString(item = "moo".toByteArray())
                                    ),
                                    Pair(
                                        BString(item = "spam".toByteArray()),
                                        BString(item = "eggs".toByteArray())
                                    )
                                )
                            )
                        )
                    )
                ) as Pair<ByteArray, LinkedHashMap<BString, BItem<*>>>
            )
        }
    }
}