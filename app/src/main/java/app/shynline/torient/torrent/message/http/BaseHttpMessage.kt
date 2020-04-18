package app.shynline.torient.torrent.message.http

import app.shynline.torient.torrent.bencoding.BDict
import app.shynline.torient.torrent.bencoding.BString
import app.shynline.torient.torrent.message.BaseMessage
import java.io.IOException
import java.nio.ByteBuffer

abstract class BaseHttpMessage(
    type: Type,
    data: ByteBuffer?
) : BaseMessage(type, data) {

    companion object {
        @Throws(IOException::class, MessageValidationException::class)
        fun parse(data: ByteBuffer): BaseHttpMessage? {
            data.rewind()
            val dataBA = ByteArray(data.remaining())
            data.get(dataBA)
            val decoded = try {
                BDict(bencoded = dataBA)
            } catch (e: Exception) {
                throw MessageValidationException("Could not decode tracker message!")
            }
            return when {
                decoded.value().containsKey(BString(item = "info_hash".toByteArray())) -> {
                    HTTPAnnounceRequestMessage.parse(data)
                }
                decoded.value().containsKey(BString(item = "peers".toByteArray())) -> {
                    HTTPAnnounceResponseMessage.parse(data)
                }
                decoded.value().containsKey(BString(item = "failure reason".toByteArray())) -> {
                    HTTPErrorMessage.parse(data)
                }
                else -> throw MessageValidationException("Unknown HTTP tracker message!")
            }
        }
    }
}