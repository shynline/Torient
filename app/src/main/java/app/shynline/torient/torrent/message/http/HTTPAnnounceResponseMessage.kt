package app.shynline.torient.torrent.message.http

import app.shynline.torient.torrent.Peer
import app.shynline.torient.torrent.bencoding.BDict
import app.shynline.torient.torrent.bencoding.BInteger
import app.shynline.torient.torrent.bencoding.BList
import app.shynline.torient.torrent.bencoding.BString
import app.shynline.torient.torrent.bencoding.common.BItem
import app.shynline.torient.torrent.bencoding.common.InvalidBencodedException
import app.shynline.torient.torrent.message.BaseMessage
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.UnknownHostException
import java.nio.ByteBuffer
import java.util.*


class HTTPAnnounceResponseMessage private constructor(
    data: ByteBuffer,
    private val interval: Int,
    private val complete: Int,
    private val incomplete: Int,
    private val peers: List<Peer>
) : BaseHTTPMessage(Type.ANNOUNCE_RESPONSE, data), BaseMessage.AnnounceResponseMessage {

    override fun getInterval(): Int {
        return interval
    }

    override fun getComplete(): Int {
        return complete
    }

    override fun getIncomplete(): Int {
        return incomplete
    }

    override fun getPeers(): List<Peer> {
        return peers
    }

    companion object {
        @Throws(IOException::class, MessageValidationException::class)
        fun parse(data: ByteBuffer): HTTPAnnounceResponseMessage? {
            data.rewind()
            val dataBA = ByteArray(data.remaining())
            data.get(dataBA)
            val decoded = try {
                BDict(bencoded = dataBA)
            } catch (e: Exception) {
                throw MessageValidationException("Could not decode tracker message!")
            }
            return try {
                val peers = parsePeers(decoded.value()[BString(item = "peers".toByteArray())]!!)
                val completeKey = BString(item = "complete".toByteArray())
                val inCompleteKey = BString(item = "incomplete".toByteArray())
                HTTPAnnounceResponseMessage(
                    data,
                    (decoded.value()[BString(item = "interval".toByteArray())] as BInteger).value()
                        .toInt(),
                    if (decoded.value().containsKey(completeKey))
                        (decoded.value()[completeKey] as BInteger).value().toInt() else 0,
                    if (decoded.value().containsKey(inCompleteKey))
                        (decoded.value()[inCompleteKey] as BInteger).value().toInt() else 0,
                    peers
                )
            } catch (e: InvalidBencodedException) {
                throw MessageValidationException("Invalid response from tracker!", e)
            } catch (e: UnknownHostException) {
                throw MessageValidationException("Invalid peer in tracker response!", e)
            }
        }

        private fun parsePeers(bItem: BItem<*>): List<Peer> {
            if (bItem is BList) {
                var bd: BDict
                return bItem.value().map {
                    bd = it as BDict
                    Peer(
                        (bd.value()[BString(item = "ip".toByteArray())] as BString).toPureString(),
                        (bd.value()[BString(item = "port".toByteArray())] as BInteger).value()
                            .toInt(),
                        (bd.value()[BString(item = "peer id".toByteArray())] as BString).value()
                    )
                }
            }
            val data = (bItem as BString).value()
            if (data.size % 6 != 0) {
                throw InvalidBencodedException(
                    "Invalid peers " +
                            "binary information string!"
                )
            }

            val result: MutableList<Peer> = LinkedList<Peer>()
            val peers = ByteBuffer.wrap(data)

            for (i in 0 until data.size / 6) {
                val ipBytes = ByteArray(4)
                peers[ipBytes]
                val ip: InetAddress = InetAddress.getByAddress(ipBytes)
                val port = 0xFF and peers.get().toInt() shl 8 or
                        (0xFF and peers.get().toInt())
                result.add(
                    Peer(
                        InetSocketAddress(
                            ip,
                            port
                        )
                    )
                )
            }
            return result
        }

        @Throws(IOException::class, UnsupportedEncodingException::class)
        fun create(
            interval: Int,
            minInterval: Int,
            trackerId: String?,
            complete: Int,
            incomplete: Int,
            peers: List<Peer>
        ): HTTPAnnounceResponseMessage? {
            val response: LinkedHashMap<BString, BItem<*>> = linkedMapOf()
            response[BString(item = "interval".toByteArray())] = BInteger(item = interval.toLong())
            response[BString(item = "complete".toByteArray())] = BInteger(item = complete.toLong())
            response[BString(item = "incomplete".toByteArray())] =
                BInteger(item = incomplete.toLong())

            val data = ByteBuffer.allocate(peers.size * 6)
            for (peer in peers) {
                val ip: ByteArray? = peer.getRawIp()
                if (ip?.size != 4) {
                    continue
                }
                data.put(ip)
                data.putShort(peer.getPort().toShort())
            }
            response[BString(item = "peers".toByteArray())] = BString(item = data.array())
            return HTTPAnnounceResponseMessage(
                ByteBuffer.wrap(BDict(item = response).encode()),
                interval, complete, incomplete, peers
            )
        }
    }
}