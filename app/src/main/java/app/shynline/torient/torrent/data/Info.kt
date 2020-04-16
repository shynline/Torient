package app.shynline.torient.torrent.bencoding

import app.shynline.torient.torrent.bencoding.common.BItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Info(
    @Json(name = "name")
    var name: String? = null,

    @Json(name = "length")
    var length: Long = 0L,

    @Json(name = "md5sum")
    var md5sum: String? = null,

    @Json(name = "files")
    var files: List<File>? = null,

    @Json(name = "piece length")
    var pieceLength: Long = 0L,

    @Json(name = "pieces")
    var pieces: String? = null,

    @Json(name = "private")
    var privateFlag: Int = 0
) {

    fun toBenCoding(): BDict {
        val dict: LinkedHashMap<BString, BItem<*>> = linkedMapOf()

        //name
        name?.let {
            dict[BString(item = "name")] =
                BString(item = it)
        }

        //length
        dict[BString(item = "length")] =
            BInteger(item = length)

        //md5sum
        md5sum?.let {
            dict[BString(item = "md5sum")] =
                BString(item = it)
        }

        //files
        files?.map { it.toBenCoding() }.let {
            dict[BString(
                item = "files"
            )] = BList(item = it)
        }

        //piece length
        dict[BString(item = "piece length")] =
            BInteger(item = pieceLength)

        //pieces
        pieces?.let {
            dict[BString(item = "pieces")] =
                BString(item = it)
        }

        //private
        dict[BString(item = "private")] =
            BInteger(item = privateFlag.toLong())

        return BDict(item = dict)
    }
}