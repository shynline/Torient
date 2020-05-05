package app.shynline.torient.transfer

import java.text.SimpleDateFormat
import java.util.*

class NotificationID {
    companion object {
        fun getID(): Int {
            val now = Date()
            return SimpleDateFormat("ddHHmmss", Locale.US).format(now).toInt()
        }
    }
}