package app.shynline.torient.torrent.message.http

import app.shynline.torient.torrent.Peer
import app.shynline.torient.torrent.bencoding.BDict
import app.shynline.torient.torrent.bencoding.BInteger
import app.shynline.torient.torrent.bencoding.BString
import app.shynline.torient.torrent.bencoding.common.BItem
import app.shynline.torient.torrent.exts.toHexString
import app.shynline.torient.torrent.message.BaseMessage
import app.shynline.torient.torrent.message.BaseMessage.AnnounceRequestMessage.RequestEvent
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder
import java.nio.ByteBuffer


class HTTPAnnounceRequestMessage private constructor(
    data: ByteBuffer,
    private val infoHash: ByteArray,
    val peer: Peer,
    private val uploaded: Long,
    private val downloaded: Long,
    private val left: Long,
    val compact: Boolean,
    val noPeerId: Boolean,
    private val event: RequestEvent,
    private val numWant: Int
) : BaseHTTPMessage(Type.ANNOUNCE_REQUEST, data), BaseMessage.AnnounceRequestMessage {

    override fun getInfoHash(): ByteArray {
        return infoHash
    }

    override fun getUploaded(): Long {
        return uploaded
    }

    override fun getDownloaded(): Long {
        return downloaded
    }

    override fun getNumWant(): Int {
        return numWant
    }

    override fun getLeft(): Long {
        return left
    }

    override fun getEvent(): RequestEvent {
        return event
    }

    override fun getHexInfoHash(): String {
        return infoHash.toHexString()
    }

    override fun getPeerId(): ByteArray? {
        return peer.peerId
    }

    override fun getHexPeerId(): String? {
        return peer.peerId?.toHexString()
    }

    override fun getPort(): Int {
        return peer.getPort()
    }

    override fun getCompact(): Boolean? {
        return compact
    }

    override fun getNoPeerId(): Boolean? {
        return noPeerId
    }

    override fun getIp(): String? {
        return peer.getIp()
    }

    @Throws(UnsupportedEncodingException::class, MalformedURLException::class)
    fun buildAnnounceURL(trackerAnnounceURL: URL): URL? {
        val base: String = trackerAnnounceURL.toString()
        return URL(buildString {
            append(base)
            append(if (base.contains("?")) "&" else "?")
            append("info_hash=")
            append(
                URLEncoder.encode(
                    String(getInfoHash(), Charsets.ISO_8859_1),
                    Charsets.ISO_8859_1.name()
                )
            )
            getPeerId()?.let {
                append("&peer_id=")
                append(
                    URLEncoder.encode(
                        String(it, Charsets.ISO_8859_1),
                        Charsets.ISO_8859_1.name()
                    )
                )
            }
            append("&port=")
            append(getPort())
            append("&uploaded=")
            append(getUploaded())
            append("&downloaded=")
            append(getDownloaded())
            append("&left=")
            append(getLeft())
            append("&compact=")
            append(if (getCompact()!!) 1 else 0)
            append("&no_peer_id=")
            append(if (getNoPeerId() == true) 1 else 0)
            if (RequestEvent.NONE != getEvent()) {
                append("&event=")
                append(getEvent().getEventName())
            }
            getIp()?.let {
                append("&ip=")
                append(it)
            }
        })
    }

    companion object {

        @Throws(IOException::class, MessageValidationException::class)
        fun parse(data: ByteBuffer): HTTPAnnounceRequestMessage? {
            data.rewind()
            val dataBA = ByteArray(data.remaining())
            data.get(dataBA)
            val decoded = try {
                BDict(bencoded = dataBA)
            } catch (e: Exception) {
                throw MessageValidationException("Could not decode tracker message!")
            }
            if (!decoded.containsKey("info_hash")) {
                throw MessageValidationException(ErrorMessage.Reason.MISSING_HASH.message)
            }
            if (!decoded.containsKey("peer_id")) {
                throw MessageValidationException(ErrorMessage.Reason.MISSING_PEER_ID.message)
            }
            if (!decoded.containsKey("port")) {
                throw MessageValidationException(ErrorMessage.Reason.MISSING_PORT.message)
            }
            return try {
                val infoHash = decoded["info_hash"]!!.getBString().value()
                val peerId = decoded["peer_id"]!!.getBString().value()
                val port = decoded["port"]!!.getBInteger().value().toInt()
                val uploaded = decoded["uploaded"]!!.getBInteger().value()
                val downloaded = decoded["downloaded"]!!.getBInteger().value()
                val left = decoded["left"]!!.getBInteger().value()
                val compact = decoded["compact"]!!.getBInteger().value() != 0L
                val noPeerId = decoded["no_peer_id"]!!.getBInteger().value() != 0L
                val numWant = decoded["numwant"]!!.getBInteger().value().toInt()
                val ip = decoded["ip"]!!.getBString().toPureString()
                val event = RequestEvent.getByName((decoded["event"] as BString).toPureString())!!
                HTTPAnnounceRequestMessage(
                    data, infoHash,
                    Peer(ip, port, peerId),
                    uploaded, downloaded, left, compact, noPeerId,
                    event, numWant
                )
            } catch (e: Exception) {
                throw MessageValidationException(
                    "Invalid HTTP tracker request!", e
                )
            }
        }

        @Throws(
            IOException::class,
            MessageValidationException::class,
            UnsupportedEncodingException::class
        )
        fun create(
            infoHash: ByteArray,
            peerId: ByteArray?,
            port: Int,
            uploaded: Long,
            downloaded: Long,
            left: Long,
            compact: Boolean,
            noPeerId: Boolean,
            event: RequestEvent?,
            ip: String?,
            numWant: Int
        ): HTTPAnnounceRequestMessage? {
            val params: LinkedHashMap<BString, BItem<*>> = linkedMapOf()
            params[BString(item = "info_hash")] = BString(item = infoHash)
            peerId?.let {
                params[BString(item = "peer_id")] = BString(item = it)
            }
            params[BString(item = "port")] = BInteger(item = port.toLong())
            params[BString(item = "uploaded")] = BInteger(item = uploaded)
            params[BString(item = "downloaded")] = BInteger(item = downloaded)
            params[BString(item = "left")] = BInteger(item = left)
            params[BString(item = "compact")] = BInteger(item = if (compact) 1 else 0)
            params[BString(item = "no_peer_id")] =
                BInteger(item = if (noPeerId) 1 else 0)
            event?.let {
                params[BString(item = "event")] =
                    BString(item = event.getEventName())
            }
            ip?.let {
                params[BString(item = "ip")] = BString(item = it)
            }
            if (numWant != AnnounceRequestMessage.DEFAULT_NUM_WANT) {
                params[BString(item = "numwant")] = BInteger(item = numWant.toLong())
            }
            return HTTPAnnounceRequestMessage(
                ByteBuffer.wrap(BDict(item = params).encode()),
                infoHash, Peer(ip, port, peerId),
                uploaded, downloaded, left, compact, noPeerId, event!!, numWant
            )
        }
    }
}