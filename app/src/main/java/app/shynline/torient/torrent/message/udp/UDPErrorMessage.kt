package app.shynline.torient.torrent.message.udp

import app.shynline.torient.torrent.message.BaseMessage

import java.io.UnsupportedEncodingException
import java.nio.ByteBuffer


class UDPErrorMessage private constructor(
    data: ByteBuffer,
    private val transactionId: Int,
    private val reason: String
) : BaseUDPMessage.Companion.BaseUDPRequestMessage.BaseUDPResponseMessage(Type.ERROR, data),
    BaseMessage.ErrorMessage {
    private val actionId: Int = Type.ERROR.id

    override fun getActionId(): Int {
        return actionId
    }

    override fun getTransactionId(): Int {
        return transactionId
    }

    override fun getReason(): String? {
        return reason
    }

    companion object {
        private const val UDP_TRACKER_ERROR_MIN_MESSAGE_SIZE = 8

        @Throws(MessageValidationException::class)
        fun parse(data: ByteBuffer): UDPErrorMessage {
            if (data.remaining() < UDP_TRACKER_ERROR_MIN_MESSAGE_SIZE) {
                throw MessageValidationException(
                    "Invalid tracker error message size!"
                )
            }
            if (data.int != Type.ERROR.id) {
                throw MessageValidationException(
                    "Invalid action code for tracker error!"
                )
            }
            val transactionId: Int = data.getInt()
            val reasonBytes = ByteArray(data.remaining())
            data.get(reasonBytes)
            return try {
                UDPErrorMessage(
                    data,
                    transactionId,
                    String(reasonBytes, Charsets.ISO_8859_1)
                )
            } catch (uee: UnsupportedEncodingException) {
                throw MessageValidationException(
                    "Could not decode error message!", uee
                )
            }
        }

        @Throws(UnsupportedEncodingException::class)
        fun create(
            transactionId: Int,
            reason: String
        ): UDPErrorMessage {
            val reasonBytes: ByteArray = reason.toByteArray()
            val data: ByteBuffer = ByteBuffer
                .allocate(UDP_TRACKER_ERROR_MIN_MESSAGE_SIZE + reasonBytes.size)
            data.putInt(Type.ERROR.id)
            data.putInt(transactionId)
            data.put(reasonBytes)
            return UDPErrorMessage(
                data,
                transactionId,
                reason
            )
        }
    }

}