package app.shynline.torient.torrent.bencoding

import app.shynline.torient.torrent.bencoding.common.BItem

data class File(
    var length: Long = 0L,

    var md5sum: String? = null,

    var path: MutableList<String> = mutableListOf()
) {

    fun toBenCoding(): BDict {
        val dict: LinkedHashMap<BString, BItem<*>> = linkedMapOf()

        dict[BString(item = "length".toByteArray())] =
            BInteger(item = length)

        md5sum?.let {
            dict[BString(item = "md5sum".toByteArray())] =
                BString(item = it.toByteArray())
        }

        path.map { BString(item = it.toByteArray()) }.let {
            dict[BString(
                item = "path".toByteArray()
            )] = BList(item = it)
        }

        return BDict(item = dict)
    }

    companion object {
        fun fromBendCoding(bDict: BDict): File {
            val file = File()
            bDict.value().forEach {
                when (it.key.toPureString()) {
                    "length" -> {
                        file.length = (it.value as BInteger).value()
                    }
                    "md5sum" -> {
                        file.md5sum = (it.value as BString).toPureString()
                    }
                    "path" -> {
                        (it.value as BList).value().forEach { path ->
                            file.path.add((path as BString).toPureString())
                        }
                    }
                }
            }
            return file
        }
    }
}