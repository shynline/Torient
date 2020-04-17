package app.shynline.torient.torrent.bencoding

import app.shynline.torient.torrent.bencoding.common.BItem

data class Info(
    var name: String? = null,
    var length: Long = 0L,
    var md5sum: String? = null,
    var files: MutableList<File>? = null,
    var pieceLength: Long = 0L,
    var pieces: ByteArray? = null,
    var privateFlag: Int = 0
) {

    fun toBenCoding(): BDict {
        val dict: LinkedHashMap<BString, BItem<*>> = linkedMapOf()

        //name
        name?.let {
            dict[BString(item = "name".toByteArray())] =
                BString(item = it.toByteArray())
        }

        //length
        dict[BString(item = "length".toByteArray())] =
            BInteger(item = length)

        //md5sum
        md5sum?.let {
            dict[BString(item = "md5sum".toByteArray())] =
                BString(item = it.toByteArray())
        }

        //files
        files?.map { it.toBenCoding() }.let {
            dict[BString(
                item = "files".toByteArray()
            )] = BList(item = it)
        }

        //piece length
        dict[BString(item = "piece length".toByteArray())] =
            BInteger(item = pieceLength)

        //pieces
        pieces?.let {
            dict[BString(item = "pieces".toByteArray())] =
                BString(item = it)
        }

        //private
        dict[BString(item = "private".toByteArray())] =
            BInteger(item = privateFlag.toLong())

        return BDict(item = dict)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Info

        if (name != other.name) return false
        if (length != other.length) return false
        if (md5sum != other.md5sum) return false
        if (files != other.files) return false
        if (pieceLength != other.pieceLength) return false
        if (pieces != null) {
            if (other.pieces == null) return false
            if (pieces?.contentEquals(other.pieces!!) != true) return false
        } else if (other.pieces != null) return false
        if (privateFlag != other.privateFlag) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + length.hashCode()
        result = 31 * result + (md5sum?.hashCode() ?: 0)
        result = 31 * result + (files?.hashCode() ?: 0)
        result = 31 * result + pieceLength.hashCode()
        result = 31 * result + (pieces?.contentHashCode() ?: 0)
        result = 31 * result + privateFlag
        return result
    }

    companion object {
        fun fromBendCoding(bDict: BDict): Info {
            val info = Info()
            bDict.value().forEach {
                when (it.key.toPureString()) {
                    "name" -> {
                        info.name = (it.value as BString).toPureString()
                    }
                    "length" -> {
                        info.length = (it.value as BInteger).value()
                    }
                    "md5sum" -> {
                        info.md5sum = (it.value as BString).toPureString()
                    }
                    "files" -> {
                        info.files = mutableListOf()
                        (it.value as BList).value().forEach { item ->
                            info.files!!.add(File.fromBendCoding(item as BDict))
                        }
                    }
                    "piece length" -> {
                        info.pieceLength = (it.value as BInteger).value()
                    }
                    "pieces" -> {
                        info.pieces = (it.value as BString).value()
                    }
                    "private" -> {
                        info.privateFlag = (it.value as BInteger).value().toInt()
                    }
                }
            }
            return info
        }

    }

}