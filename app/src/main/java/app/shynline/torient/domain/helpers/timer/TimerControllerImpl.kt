package app.shynline.torient.domain.helpers.timer

import java.util.*
import kotlin.concurrent.scheduleAtFixedRate

class TimerControllerImpl : TimerController {

    private var timer: Timer? = null
    private val timers: HashMap<Any, TimerTask> = hashMapOf()

    override fun schedule(key: Any, initialDelay: Long, period: Long, action: () -> Unit) {
        if (timers.containsKey(key)) {
            throw IllegalStateException("You can't reschedule without canceling the last one!")
        }
        if (timer == null) {
            timer = Timer("torient.timer")
        }
        val task = timer!!.scheduleAtFixedRate(
            delay = initialDelay,
            period = period
        ) {
            action()
        }
        timers[key] = task
    }

    override fun cancel(key: Any) {
        timers[key]?.cancel()
        timers.remove(key)
        if (timers.isEmpty()) {
            timer?.cancel()
            timer = null
        }
    }

}