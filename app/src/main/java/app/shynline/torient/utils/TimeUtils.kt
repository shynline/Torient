package app.shynline.torient.utils

fun Long.toReadableTime(): String {

    var time = this
    var timeF = this.toFloat()
    if (timeF > 60) {
        timeF /= 60 // Convert to Minute
        time = timeF.toLong()
    } else {
        val seconds = if (time == 1L) "" else "s"
        return "$time second$seconds remaining"
    }
    if (timeF > 60) {
        timeF /= 60 // Convert to Hour
        time = timeF.toLong()
    } else {
        val r = ((timeF - time) * 60).toLong()
        val seconds = if (r == 1L) "" else "s"
        val second = if (r > 0) ", $r second$seconds" else ""
        val minutes = if (time == 1L) "" else "s"
        return "$time minute$minutes$second remaining"
    }
    if (timeF > 24) {
        timeF /= 24 // Convert to Day
        time = timeF.toLong()
    } else {
        val r = ((timeF - time) * 60).toLong()
        val minutes = if (r == 1L) "" else "s"
        val minute = if (r > 0) ", $r minute$minutes" else ""
        val hours = if (time == 1L) "" else "s"
        return "$time hour$hours$minute remaining"
    }
    if (timeF > 365) {
        return "" // More than a year
    }
    val r = ((timeF - time) * 24).toLong()
    val hours = if (r == 1L) "" else "s"
    val hour = if (r > 0) ", $r hour$hours" else ""
    val days = if (time == 1L) "" else "s"
    return "$time day$days$hour remaining"

}