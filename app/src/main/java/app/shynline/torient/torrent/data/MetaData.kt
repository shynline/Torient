package app.shynline.torient.torrent.bencoding

import app.shynline.torient.torrent.bencoding.common.BItem
import java.security.MessageDigest

data class MetaData(
    var announce: String? = null,
    var announceList: MutableList<List<String>>? = null,
    var creationDate: Long? = null,
    var comment: String? = null,
    var createdBy: String? = null,
    var encoding: String? = null,
    var info: Info? = null
) {

    var bDict: BDict? = null
        get() {
            if (field == null)
                field = toBenCoding()
            return field
        }

    val infoHash: ByteArray by lazy { calculateInfoHash() }

    private fun calculateInfoHash(): ByteArray {
        val md = MessageDigest.getInstance("SHA-1")
        md.reset()
        md.update((bDict!!["info"] as BDict).encode())
        return md.digest()
    }

    private fun toBenCoding(): BDict {
        val dict: LinkedHashMap<BString, BItem<*>> = linkedMapOf()

        //announce
        announce?.let {
            dict[BString(item = "announce".toByteArray())] = BString(item = it.toByteArray())
        }

        //announce-list
        announceList?.map { inner ->
            BList(item = inner.map {
                BString(item = it.toByteArray())
            })
        }.let {
            dict[BString(item = "announce-list".toByteArray())] = BList(item = it)
        }

        //creation date
        creationDate?.let {
            dict[BString(item = "creation date".toByteArray())] =
                BString(item = it.toString().toByteArray())
        }

        //comment
        comment?.let {
            dict[BString(item = "comment".toByteArray())] = BString(item = it.toByteArray())
        }

        //created by
        createdBy?.let {
            dict[BString(item = "created by".toByteArray())] = BString(item = it.toByteArray())
        }

        //encoding
        encoding?.let {
            dict[BString(item = "encoding".toByteArray())] = BString(item = it.toByteArray())
        }

        //info
        info?.let { dict[BString(item = "info".toByteArray())] = it.toBenCoding() }
        return BDict(item = dict)
    }

    companion object {
        fun fromBenCoding(bDict: BDict): MetaData {
            val metaData = MetaData()
            metaData.bDict = bDict
            bDict.value().forEach {
                when (it.key.toPureString()) {
                    "announce" -> {
                        metaData.announce = (it.value as BString).toPureString()
                    }
                    "announce-list" -> {
                        metaData.announceList = mutableListOf()
                        (it.value as BList).value().forEach { items ->
                            metaData.announceList!!.add((items as BList).value().map { item ->
                                (item as BString).toPureString()
                            })
                        }
                    }
                    "creation date" -> {
                        metaData.creationDate = (it.value as BInteger).value()
                    }
                    "comment" -> {
                        metaData.comment = (it.value as BString).toPureString()
                    }
                    "created by" -> {
                        metaData.createdBy = (it.value as BString).toPureString()
                    }
                    "encoding" -> {
                        metaData.encoding = (it.value as BString).toPureString()
                    }
                    "info" -> {
                        metaData.info = Info.fromBendCoding(it.value as BDict)
                    }
                }
            }
            return metaData
        }
    }
}