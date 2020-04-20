package app.shynline.torient.torrent

import app.shynline.torient.torrent.exts.toHexString
import java.net.InetSocketAddress

open class Peer(
    private val ip: String?,
    private val port: Int,
    val peerId: ByteArray? = null
) {
    constructor(address: InetSocketAddress, peerId: ByteArray? = null)
            : this(address.hostName, address.port, peerId)

    fun getRawIp(): ByteArray? {
        return ip?.toByteArray(Charsets.ISO_8859_1)
    }

    fun getPort(): Int {
        return port
    }

    fun getIp(): String? {
        return ip
    }


    override fun equals(other: Any?): Boolean {
        if (other is Peer) {
            if (peerId != null && other.peerId != null) {
                if (!other.peerId.contentEquals(peerId)) {
                    return false
                }
            } else if (peerId != null || other.peerId != null) {
                return false
            }
            return other.getIp() == getIp() && other.getPort() == getPort()
        }
        return false
    }


    override fun toString(): String {
        return "Peer(ip=\"${getIp()}\" port=${getPort()} id=${peerId?.toHexString()})"
    }

    override fun hashCode(): Int {
        var result = ip?.hashCode() ?: 0
        result = 31 * result + port
        result = 31 * result + (peerId?.contentHashCode() ?: 0)
        return result
    }
}