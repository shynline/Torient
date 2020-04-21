package app.shynline.torient.torrent.internal.announce

import app.shynline.torient.torrent.Peer
import app.shynline.torient.torrent.internal.SharedTorrent
import app.shynline.torient.torrent.message.BaseMessage
import app.shynline.torient.torrent.message.BaseMessage.AnnounceRequestMessage.RequestEvent
import app.shynline.torient.torrent.message.BaseMessage.ErrorMessage
import app.shynline.torient.torrent.message.BaseMessage.MessageValidationException
import app.shynline.torient.torrent.message.udp.*
import app.shynline.torient.torrent.message.udp.BaseUDPMessage.ConnectionResponseMessage
import java.io.IOException
import java.net.*
import java.nio.ByteBuffer
import java.nio.channels.UnsupportedAddressTypeException
import java.util.*
import kotlin.math.pow


class UDPTrackerClient constructor(torrent: SharedTorrent?, peer: Peer, tracker: URI) :
    TrackerClient(torrent!!, peer, tracker) {
    private val address: InetSocketAddress
    private val random: Random
    private var socket: DatagramSocket?
    private var connectionExpiration: Date?
    private var connectionId: Long = 0
    private var transactionId = 0
    private var stop: Boolean

    private enum class State {
        CONNECT_REQUEST, ANNOUNCE_REQUEST
    }

    @Throws(AnnounceException::class)
    override fun announce(event: RequestEvent?, inhibitEvent: Boolean) {
        var state = State.CONNECT_REQUEST
        val maxAttempts =
            if (RequestEvent.STOPPED == event) UDP_MAX_TRIES_ON_STOPPED else UDP_MAX_TRIES
        var attempts = -1
        try {
            socket = DatagramSocket()
            socket?.connect(address)
            while (++attempts <= maxAttempts) {
                // Transaction ID is randomized for each exchange.
                transactionId = random.nextInt()

                // Immediately decide if we can send the announce request
                // directly or not. For this, we need a valid, non-expired
                // connection ID.
                if (connectionExpiration != null) {
                    if (Date().before(connectionExpiration)) {
                        state = State.ANNOUNCE_REQUEST
                    } else {

                    }
                }
                when (state) {
                    State.CONNECT_REQUEST -> {
                        send(UDPConnectRequestMessage.create(transactionId).data)
                        try {
                            handleTrackerConnectResponse(
                                UDPConnectResponseMessage.parse(
                                    recv(
                                        attempts
                                    )
                                )
                            )
                            attempts = -1
                        } catch (ste: SocketTimeoutException) {
                            // Silently ignore the timeout and retry with a
                            // longer timeout, unless announce stop was
                            // requested in which case we need to exit right
                            // away.
                            if (stop) {
                                return
                            }
                        }
                    }
                    State.ANNOUNCE_REQUEST -> {
                        send(buildAnnounceRequest(event!!)!!.data)
                        try {
                            this.handleTrackerAnnounceResponse(
                                UDPAnnounceResponseMessage.parse(recv(attempts)), inhibitEvent
                            )
                            // If we got here, we successfully completed this
                            // announce exchange and can simply return to exit the
                            // loop.
                            return
                        } catch (ste: SocketTimeoutException) {
                            // Silently ignore the timeout and retry with a
                            // longer timeout, unless announce stop was
                            // requested in which case we need to exit right
                            // away.
                            if (stop) {
                                return
                            }
                        }
                    }
                }
            }
            throw AnnounceException("Timeout while announcing" + formatAnnounceEvent(event!!) + " to tracker!")
        } catch (ioe: IOException) {
            throw AnnounceException(
                "Error while announcing" +
                        formatAnnounceEvent(event!!) +
                        " to tracker: " + ioe.message, ioe
            )
        } catch (mve: MessageValidationException) {
            throw AnnounceException(
                "Tracker message violates expected " +
                        "protocol (" + mve.message + ")", mve
            )
        }
    }

    @Throws(AnnounceException::class)
    override fun handleTrackerAnnounceResponse(
        message: BaseMessage,
        inhibitEvents: Boolean
    ) {
        validateTrackerResponse(message)
        super.handleTrackerAnnounceResponse(message, inhibitEvents)
    }


    override fun close() {
        stop = true

        // Close the socket to force blocking operations to return.
        if (socket != null && !socket!!.isClosed) {
            socket!!.close()
        }
    }

    private fun buildAnnounceRequest(
        event: RequestEvent
    ): UDPAnnounceRequestMessage? {
        return UDPAnnounceRequestMessage.craft(
            connectionId,
            transactionId,
            torrent.infoHash,
            peer.peerId!!,
            0, 0, torrent.size,
            event, 0, BaseMessage.AnnounceRequestMessage.DEFAULT_NUM_WANT,
//            torrent.getUploaded(),
//            torrent.getDownloaded(),
//            torrent.getLeft(),
            peer.getPort()
        )
    }


    @Throws(AnnounceException::class)
    private fun validateTrackerResponse(message: BaseMessage) {
        if (message is ErrorMessage) {
            throw AnnounceException((message as ErrorMessage).getReason())
        }
        if (message is BaseUDPMessage &&
            message.getTransactionId() != transactionId
        ) {
            throw AnnounceException("Invalid transaction ID!")
        }
    }


    @Throws(AnnounceException::class)
    private fun handleTrackerConnectResponse(message: BaseMessage) {
        validateTrackerResponse(message)
        if (message !is ConnectionResponseMessage) {
            throw AnnounceException(
                "Unexpected tracker message type " +
                        message.type.name + "!"
            )
        }
        val connectResponse = message as UDPConnectResponseMessage
        connectionId = connectResponse.connectionId
        val now: Calendar = Calendar.getInstance()
        now.add(Calendar.MINUTE, 1)
        connectionExpiration = now.time
    }


    private fun send(data: ByteBuffer?) {
        try {
            socket?.send(
                DatagramPacket(
                    data!!.array(),
                    data.capacity(),
                    address
                )
            )
        } catch (ioe: IOException) {

        }
    }


    @Throws(IOException::class, SocketException::class, SocketTimeoutException::class)
    private fun recv(attempt: Int): ByteBuffer {
        val timeout = UDP_BASE_TIMEOUT_SECONDS * 2.0.pow(attempt.toDouble()).toInt()
        socket?.soTimeout = timeout * 1000
        return try {
            val p = DatagramPacket(
                ByteArray(UDP_PACKET_LENGTH),
                UDP_PACKET_LENGTH
            )
            socket?.receive(p)
            ByteBuffer.wrap(p.data, 0, p.length)
        } catch (ste: SocketTimeoutException) {
            throw ste
        }
    }

    companion object {

        /**
         * Back-off timeout uses 15 * 2 ^ n formula.
         */
        private const val UDP_BASE_TIMEOUT_SECONDS = 15

        /**
         * We don't try more than 8 times (3840 seconds, as per the formula defined
         * for the backing-off timeout.
         *
         * @see .UDP_BASE_TIMEOUT_SECONDS
         */
        private const val UDP_MAX_TRIES = 8

        /**
         * For STOPPED announce event, we don't want to be bothered with waiting
         * that long. We'll try once and bail-out early.
         */
        private const val UDP_MAX_TRIES_ON_STOPPED = 1

        /**
         * Maximum UDP packet size expected, in bytes.
         *
         * The biggest packet in the exchange is the announce response, which in 20
         * bytes + 6 bytes per peer. Common numWant is 50, so 20 + 6 * 50 = 320.
         * With headroom, we'll ask for 512 bytes.
         */
        private const val UDP_PACKET_LENGTH = 512
    }


    init {
        /**
         * The UDP announce request protocol only supports IPv4
         *
         * @see http://bittorrent.org/beps/bep_0015.html.ipv6
         */
        if (InetAddress.getByName(peer.getIp()) !is Inet4Address) {
            throw UnsupportedAddressTypeException()
        }
        address = InetSocketAddress(tracker.host, tracker.port)
        socket = null
        random = Random()
        connectionExpiration = null
        stop = false
    }
}