package app.shynline.torient.torrent

import java.net.InetSocketAddress

class Peer(var address: InetSocketAddress, val peerId: ByteArray? = null) {
    constructor(ip: String?, port: Int, peerId: ByteArray? = null)
            : this(InetSocketAddress(ip, port), peerId)

    fun getRawIp(): ByteArray? {
        return this.address.address.address
    }

    fun getPort(): Int {
        return address.port
    }

    fun getIp(): String? {
        return address.address.hostAddress
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
            return other.address.hostName == address.hostName && other.address.port == address.port
        }
        return false
    }

    override fun hashCode(): Int {
        var result = address.hashCode()
        result = 31 * result + (peerId?.contentHashCode() ?: 0)
        return result
    }

}