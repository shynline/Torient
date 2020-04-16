package app.shynline.torient.torrent.bencoding

import app.shynline.torient.torrent.bencoding.common.BItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi

@JsonClass(generateAdapter = true)
data class MetaData(
    @Json(name = "announce")
    var announce: String? = null,

    @Json(name = "announce-list")
    var announceList: List<List<String>>? = null,

    @Json(name = "creation date")
    var creationDate: String? = null,

    @Json(name = "comment")
    var comment: String? = null,

    @Json(name = "created by")
    var createdBy: String? = null,

    @Json(name = "encoding")
    var encoding: String? = null,

    @Json(name = "info")
    var info: Info? = null
) {

    fun toBenCoding(): BDict {
        val dict: LinkedHashMap<BString, BItem<*>> = linkedMapOf()

        //announce
        announce?.let { dict[BString(item = "announce")] = BString(item = it) }

        //announce-list
        announceList?.map { inner ->
            BList(item = inner.map {
                BString(item = it)
            })
        }.let {
            dict[BString(item = "announce-list")] = BList(item = it)
        }

        //creation date
        creationDate?.let { dict[BString(item = "creation date")] = BString(item = it) }

        //comment
        comment?.let { dict[BString(item = "comment")] = BString(item = it) }

        //created by
        createdBy?.let { dict[BString(item = "created by")] = BString(item = it) }

        //encoding
        encoding?.let { dict[BString(item = "encoding")] = BString(item = it) }

        //info
        info?.let { dict[BString(item = "info")] = it.toBenCoding() }
        return BDict(item = dict)
    }

    companion object {
        fun fromBenCoding(bDict: BDict): MetaData {
            val moshi = Moshi.Builder().build()
            val jsonAdapter: JsonAdapter<MetaData> =
                moshi.adapter(MetaData::class.java)
            return jsonAdapter.fromJson(bDict.toString(false))!!
        }
    }
}