package app.shynline.torient.torrent.message

import app.shynline.torient.torrent.Peer
import java.nio.ByteBuffer
import java.util.*


abstract class BaseMessage(
    val type: Type,
    val data: ByteBuffer?
) {
    enum class Type(val id: Int) {
        UNKNOWN(-1),
        CONNECT_REQUEST(0),
        CONNECT_RESPONSE(0),
        ANNOUNCE_REQUEST(1),
        ANNOUNCE_RESPONSE(1),
        ERROR(3)
    }

    init {
        data?.rewind()
    }

    interface AnnounceRequestMessage {
        companion object {
            const val DEFAULT_NUM_WANT = 50
        }

        enum class RequestEvent(val id: Int) {
            NONE(0),
            COMPLETED(1),
            STARTED(2),
            STOPPED(3);

            fun getEventName(): String {
                return name.toLowerCase(Locale.ROOT)
            }

            companion object {
                fun getByName(name: String?): RequestEvent? {
                    for (type in values()) {
                        if (type.name.equals(name, ignoreCase = true)) {
                            return type
                        }
                    }
                    return null
                }

                fun getById(id: Int): RequestEvent? {
                    for (type in values()) {
                        if (type.id == id) {
                            return type
                        }
                    }
                    return null
                }
            }
        }

        fun getInfoHash(): ByteArray
        fun getHexInfoHash(): String
        fun getPeerId(): ByteArray?
        fun getHexPeerId(): String?
        fun getPort(): Int
        fun getUploaded(): Long
        fun getDownloaded(): Long
        fun getLeft(): Long
        fun getCompact(): Boolean?
        fun getNoPeerId(): Boolean?
        fun getEvent(): RequestEvent
        fun getIp(): String?
        fun getNumWant(): Int
    }

    interface AnnounceResponseMessage {
        fun getInterval(): Int
        fun getComplete(): Int
        fun getIncomplete(): Int
        fun getPeers(): List<Peer>
    }

    interface ErrorMessage {

        enum class Reason(val message: String) {
            UNKNOWN_TORRENT("The requested torrent does not exist!"),
            MISSING_HASH("Missing info-hash!"),
            MISSING_PEER_ID("Missing peer-id!"),
            MISSING_PORT("Missing port!"),
            INVALID_EVENT("Unexpected event for peer state!"),
            NOT_IMPLEMENTED("Not implemented!");

        }

        fun getReason(): String?
    }

    class MessageValidationException : Exception {
        constructor(s: String?) : super(s)
        constructor(s: String?, cause: Throwable?) : super(s, cause)

        companion object {
            const val serialVersionUID: Long = -1
        }
    }


}