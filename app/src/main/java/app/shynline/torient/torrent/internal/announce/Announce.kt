package app.shynline.torient.torrent.internal.announce

import app.shynline.torient.torrent.internal.threadpool.ThreadPoolInterface
import app.shynline.torient.torrent.utils.gcd
import java.util.*

internal class Announce(
    private val threadPool: ThreadPoolInterface
) : Runnable {
    private val listeners: HashMap<String, AnnounceListener> = hashMapOf()
    private val intervals: HashMap<String, Int> = hashMapOf()
    private val UID = UUID.randomUUID().toString()
    private var running = false
    private var interval = DEFAULT_INTERVAL

    companion object {
        private const val DEFAULT_INTERVAL: Int = 10
    }

    fun register(identifier: String, listener: AnnounceListener) {
        listeners[identifier] = listener
    }

    fun unregister(identifier: String) {
        listeners.remove(identifier)
        intervals.remove(identifier)
        calculateInterval()
    }

    fun setInterval(infoHashHex: String, interval: Int) {
        intervals[infoHashHex] = interval
        calculateInterval()
    }

    private fun calculateInterval() {
        interval = if (intervals.isEmpty()) {
            DEFAULT_INTERVAL
        } else {
            gcd(intervals.values.toList())
        }
    }

    fun start() {
        running = true
        threadPool.runOnDedicatedThread(UID, this)
    }

    fun stop() {
        running = false
        threadPool.stopDedicatedThread(UID)
    }

    override fun run() {
        var timeFrame = 0
        while (running) {
            listeners.forEach {
                it.value.announce(timeFrame)
            }
            try {
                timeFrame = interval
                Thread.sleep(interval * 1000L)
            } catch (e: InterruptedException) {
                break
            }
        }

    }
}