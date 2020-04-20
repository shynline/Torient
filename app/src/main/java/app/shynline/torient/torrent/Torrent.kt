package app.shynline.torient.torrent

import app.shynline.torient.torrent.bencoding.MetaData
import app.shynline.torient.torrent.exts.toHexString


open class Torrent internal constructor(
    val metaData: MetaData,
    val seed: Boolean
) {

    val infoHashHex: String
    val infoHash: ByteArray
    val size: Long

    var complete = 0
        protected set
    var inComplete = 0
        protected set


    init {
        infoHash = metaData.infoHash
        infoHashHex = infoHash.toHexString()

        var lSize = metaData.info!!.pieceLength
        metaData.info!!.files?.forEach {
            lSize += it.length
        }
        size = lSize

    }

    val name: String
        get() {
            return metaData.info!!.name!!
        }
    val comment: String
        get() {
            return metaData.comment!!
        }

    val createdBy: String
        get() {
            return metaData.createdBy!!
        }


    companion object {
        const val PIECE_HASH_SIZE = 20
    }

}

