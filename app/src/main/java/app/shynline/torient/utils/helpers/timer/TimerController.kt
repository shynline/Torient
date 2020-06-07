package app.shynline.torient.utils.helpers.timer

interface TimerController {
    fun schedule(key: Any, initialDelay: Long, period: Long, action: () -> Unit)
    fun cancel(key: Any)
}