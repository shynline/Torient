package app.shynline.torient.torrent.internal.threadpool

import java.util.concurrent.*

internal class ThreadPool :
    ThreadPoolInterface {
    private var executor: ExecutorService? = null
    private var threads: HashMap<String, Thread> = hashMapOf()

    fun onStart() {
        executor?.shutdownNow()
        executor = ThreadPoolExecutor(
            10, 100, 10, TimeUnit.SECONDS,
            LinkedBlockingQueue(),
            TPThreadFactory()
        )

    }

    fun onStop() {
        executor?.shutdownNow()
        threads.forEach {
            if (it.value.isAlive)
                it.value.interrupt()
        }
        threads.clear()
    }

    override fun run(runnable: Runnable) {
        executor?.submit(runnable)
    }

    override fun runOnDedicatedThread(UID: String, runnable: Runnable) {
        stopDedicatedThread(UID)
        threads[UID] = Thread(runnable).apply {
            name = "Torient-server-$UID"
            start()
        }
    }

    override fun stopDedicatedThread(UID: String) {
        threads[UID]?.apply {
            if (isAlive)
                interrupt()
        }
        threads.remove(UID)
    }


    private class TPThreadFactory : ThreadFactory {
        private var tag = 0
        override fun newThread(r: Runnable): Thread {
            return Thread(r).apply { name = "Torient-TP-#${tag++}" }
        }
    }
}