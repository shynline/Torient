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


class HTTPAnnounceRequestMessage constructor(
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
            if (!decoded.value().containsKey(BString(item = "info_hash".toByteArray()))) {
                throw MessageValidationException(ErrorMessage.Reason.MISSING_HASH.message)
            }
            if (!decoded.value().containsKey(BString(item = "peer_id".toByteArray()))) {
                throw MessageValidationException(ErrorMessage.Reason.MISSING_PEER_ID.message)
            }
            if (!decoded.value().containsKey(BString(item = "port".toByteArray()))) {
                throw MessageValidationException(ErrorMessage.Reason.MISSING_PORT.message)
            }
            return try {
                var infoHash: ByteArray? = null
                var peerId: ByteArray? = null
                var port = 0
                var uploaded = 0L
                var downloaded = 0L
                var left = -1L
                var compact = false
                var noPeerId = false
                var numWant: Int = AnnounceRequestMessage.DEFAULT_NUM_WANT
                var ip: String? = null
                var event: RequestEvent = RequestEvent.NONE
                decoded.value().forEach {
                    when (it.key.toPureString()) {
                        "info_hash" -> {
                            infoHash = (it.value as BString).value()
                        }
                        "peer_id" -> {
                            peerId = (it.value as BString).value()
                        }
                        "port" -> {
                            port = (it.value as BInteger).value().toInt()
                        }
                        "uploaded" -> {
                            uploaded = (it.value as BInteger).value()
                        }
                        "downloaded" -> {
                            downloaded = (it.value as BInteger).value()
                        }
                        "left" -> {
                            left = (it.value as BInteger).value()
                        }
                        "compact" -> {
                            compact = (it.value as BInteger).value() != 0L
                        }
                        "no_peer_id" -> {
                            noPeerId = (it.value as BInteger).value() != 0L
                        }
                        "numwant" -> {
                            numWant = (it.value as BInteger).value().toInt()
                        }
                        "ip" -> {
                            ip = (it.value as BString).toPureString()
                        }
                        "event" -> {
                            event = RequestEvent.getByName((it.value as BString).toPureString())!!
                        }

                    }
                }
                HTTPAnnounceRequestMessage(
                    data, infoHash!!,
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
            params[BString(item = "info_hash".toByteArray())] = BString(item = infoHash)
            peerId?.let {
                params[BString(item = "peer_id".toByteArray())] = BString(item = it)
            }
            params[BString(item = "port".toByteArray())] = BInteger(item = port.toLong())
            params[BString(item = "uploaded".toByteArray())] = BInteger(item = uploaded)
            params[BString(item = "downloaded".toByteArray())] = BInteger(item = downloaded)
            params[BString(item = "left".toByteArray())] = BInteger(item = left)
            params[BString(item = "compact".toByteArray())] = BInteger(item = if (compact) 1 else 0)
            params[BString(item = "no_peer_id".toByteArray())] =
                BInteger(item = if (noPeerId) 1 else 0)
            event?.let {
                params[BString(item = "event".toByteArray())] =
                    BString(item = event.getEventName().toByteArray())
            }
            ip?.let {
                params[BString(item = "ip".toByteArray())] = BString(item = it.toByteArray())
            }
            if (numWant != AnnounceRequestMessage.DEFAULT_NUM_WANT) {
                params[BString(item = "numwant".toByteArray())] = BInteger(item = numWant.toLong())
            }
            return HTTPAnnounceRequestMessage(
                ByteBuffer.wrap(BDict(item = params).encode()),
                infoHash, Peer(ip, port, peerId),
                uploaded, downloaded, left, compact, noPeerId, event!!, numWant
            )
        }
    }
}