package app.shynline.torient.torrent.internal.threadpool

internal interface ThreadPoolInterface {
    fun runOnDedicatedThread(UID: String, runnable: Runnable)
    fun run(runnable: Runnable)
    fun stopDedicatedThread(UID: String)
}