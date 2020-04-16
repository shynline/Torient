package app.shynline.torient.torrent.bencoding

import app.shynline.torient.torrent.bencoding.common.BItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class File(
    @Json(name = "length")
    var length: Long = 0L,

    @Json(name = "md5sum")
    var md5sum: String? = null,

    @Json(name = "path")
    var path: List<String> = listOf()
) {

    fun toBenCoding(): BDict {
        val dict: LinkedHashMap<BString, BItem<*>> = linkedMapOf()

        dict[BString(item = "length")] =
            BInteger(item = length)

        md5sum?.let {
            dict[BString(item = "md5sum")] =
                BString(item = it)
        }

        path.map { BString(item = it) }.let {
            dict[BString(
                item = "path"
            )] = BList(item = it)
        }

        return BDict(item = dict)
    }
}