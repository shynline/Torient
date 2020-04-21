package app.shynline.torient.torrent.message.udp

import app.shynline.torient.torrent.exts.toHexString
import app.shynline.torient.torrent.message.BaseMessage
import app.shynline.torient.torrent.message.BaseMessage.AnnounceRequestMessage.RequestEvent
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.ByteBuffer

class UDPAnnounceRequestMessage(
    data: ByteBuffer,
    val connectionId: Long,
    private val transactionId: Int,
    private val infoHash: ByteArray,
    private val peerId: ByteArray?,
    private val downloaded: Long,
    private val uploaded: Long,
    private val left: Long,
    private val event: RequestEvent,
    val key: Int,
    private val numWant: Int,
    private val port: Int
) :
    BaseUDPMessage.Companion.BaseUDPRequestMessage(Type.ANNOUNCE_REQUEST, data),
    BaseMessage.AnnounceRequestMessage {
    private val actionId = Type.ANNOUNCE_REQUEST.id

    override fun getActionId(): Int {
        return actionId
    }

    override fun getTransactionId(): Int {
        return transactionId
    }

    override fun getInfoHash(): ByteArray {
        return infoHash
    }

    override fun getHexInfoHash(): String {
        return infoHash.toHexString()
    }

    override fun getPeerId(): ByteArray? {
        return peerId
    }

    override fun getHexPeerId(): String? {
        return peerId?.toHexString()
    }

    override fun getPort(): Int {
        return port
    }

    override fun getUploaded(): Long {
        return uploaded
    }

    override fun getDownloaded(): Long {
        return downloaded
    }

    override fun getLeft(): Long {
        return left
    }

    override fun getCompact(): Boolean? {
        return true
    }

    override fun getNoPeerId(): Boolean? {
        return true
    }

    override fun getEvent(): AnnounceRequestMessage.RequestEvent {
        return event
    }

    override fun getIp(): String? {
        return null
    }

    override fun getNumWant(): Int {
        return numWant
    }

    companion object {
        private const val UDP_ANNOUNCE_REQUEST_MESSAGE_SIZE = 98

        @Throws(MessageValidationException::class)
        fun parse(data: ByteBuffer): UDPAnnounceRequestMessage? {
            if (data.remaining() != UDP_ANNOUNCE_REQUEST_MESSAGE_SIZE) {
                throw MessageValidationException(
                    "Invalid announce request message size!"
                )
            }
            val connectionId = data.long
            if (data.int != Type.ANNOUNCE_REQUEST.id) {
                throw MessageValidationException(
                    "Invalid action code for announce request!"
                )
            }
            val transactionId = data.int
            val infoHash = ByteArray(20)
            data[infoHash]
            val peerId = ByteArray(20)
            data[peerId]
            val downloaded = data.long
            val uploaded = data.long
            val left = data.long
            val event = RequestEvent.getById(data.int)
                ?: throw MessageValidationException(
                    "Invalid event type in announce request!"
                )
            val ip = try {
                val ipBytes = ByteArray(4)
                data[ipBytes]
                InetAddress.getByAddress(ipBytes)
            } catch (e: UnknownHostException) {
                throw MessageValidationException("Invalid IP address in announce request!")
            }

            val key = data.int
            val numWant = data.int
            val port = data.short
            return UDPAnnounceRequestMessage(
                data,
                connectionId,
                transactionId,
                infoHash,
                peerId,
                downloaded,
                uploaded,
                left,
                event,
//                ip,
                key,
                numWant,
                port.toInt()
            )
        }

        fun craft(
            connectionId: Long,
            transactionId: Int,
            infoHash: ByteArray,
            peerId: ByteArray,
            downloaded: Long,
            uploaded: Long,
            left: Long,
            event: RequestEvent,
            key: Int,
            numWant: Int,
            port: Int
        ): UDPAnnounceRequestMessage? {
            require(!(infoHash.size != 20 || peerId.size != 20))
            val data = ByteBuffer.allocate(UDP_ANNOUNCE_REQUEST_MESSAGE_SIZE)
            data.putLong(connectionId)
            data.putInt(Type.ANNOUNCE_REQUEST.id)
            data.putInt(transactionId)
            data.put(infoHash)
            data.put(peerId)
            data.putLong(downloaded)
            data.putLong(left)
            data.putLong(uploaded)
            data.putInt(event.id)
            data.put(0)
            data.putInt(key)
            data.putInt(numWant)
            data.putShort(port.toShort())
            return UDPAnnounceRequestMessage(
                data,
                connectionId,
                transactionId,
                infoHash,
                peerId,
                downloaded,
                uploaded,
                left,
                event,
                key,
                numWant,
                port
            )
        }
    }
}