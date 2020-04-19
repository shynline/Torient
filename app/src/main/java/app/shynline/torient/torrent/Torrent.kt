package app.shynline.torient.torrent

import app.shynline.torient.torrent.bencoding.BString
import app.shynline.torient.torrent.bencoding.MetaData
import app.shynline.torient.torrent.exts.toHexString
import java.security.MessageDigest


open class Torrent(
    val metaData: MetaData,
    val seed: Boolean
) {

    val infoHashHex: String
    val infoHash: ByteArray
    val size: Long


    init {
        infoHash = calculateInfoHash()
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

    private fun calculateInfoHash(): ByteArray {
        val md = MessageDigest.getInstance("SHA-1")
        md.reset()
        md.update((metaData.bDict!!["info"] as BString).encode())
        return md.digest()
    }


    companion object {
        const val PIECE_HASH_SIZE = 20
    }

}

