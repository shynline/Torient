package app.shynline.torient.torrent.message.udp

import app.shynline.torient.torrent.message.BaseMessage
import app.shynline.torient.torrent.message.BaseMessage.MessageValidationException
import java.nio.ByteBuffer


abstract class BaseUDPMessage(
    type: Type,
    data: ByteBuffer
) : BaseMessage(type, data) {

    abstract fun getActionId(): Int
    abstract fun getTransactionId(): Int

    companion object {
        abstract class BaseUDPRequestMessage(type: Type, data: ByteBuffer) : BaseUDPMessage(
            type,
            data
        ) {
            companion object {
                private const val UDP_MIN_REQUEST_PACKET_SIZE = 16

                @Throws(MessageValidationException::class)
                fun parse(data: ByteBuffer): BaseUDPRequestMessage? {
                    if (data.remaining() < UDP_MIN_REQUEST_PACKET_SIZE) {
                        throw MessageValidationException("Invalid packet size!")
                    }
                    data.mark()
                    data.long
                    val action = data.int
                    data.reset()
                    if (action == Type.CONNECT_REQUEST.id) {
                        return UDPConnectRequestMessage.parse(data)
                    } else if (action == Type.ANNOUNCE_REQUEST.id) {
                        return UDPAnnounceRequestMessage.parse(data)
                    }
                    throw MessageValidationException("Unknown UDP tracker request message!")
                }
            }

            abstract class BaseUDPResponseMessage(
                type: Type,
                data: ByteBuffer
            ) : BaseUDPMessage(type, data) {

                companion object {
                    private const val UDP_MIN_RESPONSE_PACKET_SIZE = 8

                    @Throws(MessageValidationException::class)
                    fun parse(data: ByteBuffer): BaseUDPResponseMessage? {
                        if (data.remaining() < UDP_MIN_RESPONSE_PACKET_SIZE) {
                            throw MessageValidationException("Invalid packet size!")
                        }
                        data.mark()
                        val action = data.int
                        data.reset()
                        return when (action) {
                            Type.CONNECT_RESPONSE.id -> {
                                UDPConnectResponseMessage.parse(data)
                            }
                            Type.ANNOUNCE_RESPONSE.id -> {
                                UDPAnnounceResponseMessage.parse(data)
                            }
                            Type.ERROR.id -> {
                                UDPErrorMessage.parse(data)
                            }
                            else -> throw MessageValidationException("Unknown UDP tracker response message!")
                        }
                    }
                }
            }
        }
    }

    interface ConnectionRequestMessage
    interface ConnectionResponseMessage
}