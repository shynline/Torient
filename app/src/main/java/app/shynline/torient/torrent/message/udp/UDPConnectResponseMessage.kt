package app.shynline.torient.torrent.message.udp

import java.nio.ByteBuffer


class UDPConnectResponseMessage private constructor(
    data: ByteBuffer,
    private val transactionId: Int,
    val connectionId: Long
) : BaseUDPMessage.Companion.BaseUDPRequestMessage.BaseUDPResponseMessage(
    Type.CONNECT_RESPONSE,
    data
),
    BaseUDPMessage.ConnectionResponseMessage {
    private val actionId: Int = Type.CONNECT_RESPONSE.id

    override fun getActionId(): Int {
        return actionId
    }

    override fun getTransactionId(): Int {
        return transactionId
    }

    companion object {
        private const val UDP_CONNECT_RESPONSE_MESSAGE_SIZE = 16

        @Throws(MessageValidationException::class)
        fun parse(data: ByteBuffer): UDPConnectResponseMessage {
            if (data.remaining() != UDP_CONNECT_RESPONSE_MESSAGE_SIZE) {
                throw MessageValidationException(
                    "Invalid connect response message size!"
                )
            }
            if (data.int != Type.CONNECT_RESPONSE.id) {
                throw MessageValidationException(
                    "Invalid action code for connection response!"
                )
            }
            return UDPConnectResponseMessage(
                data,
                data.int,  // transactionId
                data.long // connectionId
            )
        }

        fun create(
            transactionId: Int,
            connectionId: Long
        ): UDPConnectResponseMessage {
            val data: ByteBuffer = ByteBuffer
                .allocate(UDP_CONNECT_RESPONSE_MESSAGE_SIZE)
            data.putInt(Type.CONNECT_RESPONSE.id)
            data.putInt(transactionId)
            data.putLong(connectionId)
            return UDPConnectResponseMessage(
                data,
                transactionId,
                connectionId
            )
        }
    }

}