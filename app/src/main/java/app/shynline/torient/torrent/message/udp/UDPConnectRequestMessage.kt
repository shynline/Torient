package app.shynline.torient.torrent.message.udp

import java.nio.ByteBuffer


class UDPConnectRequestMessage private constructor(
    data: ByteBuffer,
    private val transactionId: Int
) :
    BaseUDPMessage.Companion.BaseUDPRequestMessage(Type.CONNECT_REQUEST, data),
    BaseUDPMessage.ConnectionRequestMessage {
    val connectionId = UDP_CONNECT_REQUEST_MAGIC
    private val actionId: Int = Type.CONNECT_REQUEST.id

    override fun getActionId(): Int {
        return actionId
    }

    override fun getTransactionId(): Int {
        return transactionId
    }

    companion object {
        private const val UDP_CONNECT_REQUEST_MESSAGE_SIZE = 16
        private const val UDP_CONNECT_REQUEST_MAGIC = 0x41727101980L

        @Throws(MessageValidationException::class)
        fun parse(data: ByteBuffer): UDPConnectRequestMessage {
            if (data.remaining() != UDP_CONNECT_REQUEST_MESSAGE_SIZE) {
                throw MessageValidationException("Invalid connect request message size!")
            }
            if (data.long != UDP_CONNECT_REQUEST_MAGIC) {
                throw MessageValidationException("Invalid connection ID in connection request!")
            }
            if (data.int != Type.CONNECT_REQUEST.id) {
                throw MessageValidationException("Invalid action code for connection request!")
            }
            return UDPConnectRequestMessage(
                data,
                data.int // transaction id
            )
        }

        fun create(transactionId: Int): UDPConnectRequestMessage {
            val data: ByteBuffer = ByteBuffer
                .allocate(UDP_CONNECT_REQUEST_MESSAGE_SIZE)
            data.putLong(UDP_CONNECT_REQUEST_MAGIC)
            data.putInt(Type.CONNECT_REQUEST.id)
            data.putInt(transactionId)
            return UDPConnectRequestMessage(
                data,
                transactionId
            )
        }
    }

}