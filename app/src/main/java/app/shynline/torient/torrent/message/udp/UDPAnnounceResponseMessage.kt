package app.shynline.torient.torrent.message.udp

import app.shynline.torient.torrent.Peer
import app.shynline.torient.torrent.logger
import app.shynline.torient.torrent.message.BaseMessage
import java.net.UnknownHostException
import java.nio.ByteBuffer
import java.util.*


class UDPAnnounceResponseMessage private constructor(
    data: ByteBuffer,
    private val transactionId: Int,
    private val interval: Int,
    private val complete: Int,
    private val incomplete: Int,
    private val peers: List<Peer>
) : BaseUDPMessage.Companion.BaseUDPRequestMessage.BaseUDPResponseMessage(
    Type.ANNOUNCE_RESPONSE,
    data
),
    BaseMessage.AnnounceResponseMessage {
    private val actionId = Type.ANNOUNCE_RESPONSE.id

    override fun getActionId(): Int {
        return actionId
    }

    override fun getTransactionId(): Int {
        return transactionId
    }

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
        private const val UDP_ANNOUNCE_RESPONSE_MIN_MESSAGE_SIZE = 20

        @Throws(MessageValidationException::class)
        fun parse(data: ByteBuffer): UDPAnnounceResponseMessage {
            if (data.remaining() < UDP_ANNOUNCE_RESPONSE_MIN_MESSAGE_SIZE ||
                (data.remaining() - UDP_ANNOUNCE_RESPONSE_MIN_MESSAGE_SIZE) % 6 != 0
            ) {
                throw MessageValidationException(
                    "Invalid announce response message size!"
                )
            }
            if (data.int != Type.ANNOUNCE_RESPONSE.id) {
                throw MessageValidationException(
                    "Invalid action code for announce response!"
                )
            }
            val transactionId: Int = data.int
            val interval: Int = data.int
            val incomplete: Int = data.int
            val complete: Int = data.int
            val peers: MutableList<Peer> = LinkedList<Peer>()

            val ipBytes = ByteArray(data.remaining())
            val temp = IntArray(4)
            var port: Int
            data.get(ipBytes)
            var index = 0
            var ipp = 0
            while (index + 5 < ipBytes.size) {
                try {
                    temp[0] = ipBytes[index].toInt() and 0xFF
                    temp[1] = ipBytes[index + 1].toInt() and 0xFF
                    temp[2] = ipBytes[index + 2].toInt() and 0xFF
                    temp[3] = ipBytes[index + 3].toInt() and 0xFF
                    port =
                        0xFF and ipBytes[index + 4].toInt() shl 8 or (0xFF and ipBytes[index + 5].toInt())
                    peers.add(Peer("${temp[0]}.${temp[1]}.${temp[2]}.${temp[3]}", port, null))
                    index += 6
                    logger(ipp.toString())
                    ipp += 1
                } catch (uhe: UnknownHostException) {
                    throw MessageValidationException("Invalid IP address in announce request!")
                }
            }
            return UDPAnnounceResponseMessage(
                data,
                transactionId,
                interval,
                complete,
                incomplete,
                peers
            )
        }

        fun create(
            transactionId: Int,
            interval: Int, complete: Int, incomplete: Int, peers: List<Peer>
        ): UDPAnnounceResponseMessage {
            val data: ByteBuffer = ByteBuffer
                .allocate(UDP_ANNOUNCE_RESPONSE_MIN_MESSAGE_SIZE + 6 * peers.size)
            data.putInt(Type.ANNOUNCE_RESPONSE.id)
            data.putInt(transactionId)
            data.putInt(interval)
            data.putInt(incomplete)
            data.putInt(complete)
            for (peer in peers) {
                val ip = peer.getRawIp()
                if (ip == null || ip.size != 4) {
                    continue
                }
                data.put(ip)
                data.putShort(peer.getPort().toShort())
            }
            return UDPAnnounceResponseMessage(
                data,
                transactionId,
                interval,
                complete,
                incomplete,
                peers
            )
        }
    }

}
