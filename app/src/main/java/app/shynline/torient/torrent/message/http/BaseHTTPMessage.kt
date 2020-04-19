package app.shynline.torient.torrent.message.http

import app.shynline.torient.torrent.bencoding.BDict
import app.shynline.torient.torrent.message.BaseMessage
import java.io.IOException
import java.nio.ByteBuffer

abstract class BaseHTTPMessage(
    type: Type,
    data: ByteBuffer?
) : BaseMessage(type, data) {

    companion object {
        @Throws(IOException::class, MessageValidationException::class)
        fun parse(data: ByteBuffer): BaseHTTPMessage? {
            data.rewind()
            val dataBA = ByteArray(data.remaining())
            data.get(dataBA)
            val decoded = try {
                BDict(bencoded = dataBA)
            } catch (e: Exception) {
                throw MessageValidationException("Could not decode tracker message!")
            }
            return when {
                decoded.containsKey("info_hash") -> {
                    HTTPAnnounceRequestMessage.parse(data)
                }
                decoded.containsKey("peers") -> {
                    HTTPAnnounceResponseMessage.parse(data)
                }
                decoded.containsKey("failure reason") -> {
                    HTTPErrorMessage.parse(data)
                }
                else -> throw MessageValidationException("Unknown HTTP tracker message!")
            }
        }
    }
}