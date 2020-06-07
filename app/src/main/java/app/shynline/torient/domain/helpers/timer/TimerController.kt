package app.shynline.torient.domain.helpers.timer

interface TimerController {
    fun schedule(key: Any, initialDelay: Long, period: Long, action: () -> Unit)
    fun cancel(key: Any)
}