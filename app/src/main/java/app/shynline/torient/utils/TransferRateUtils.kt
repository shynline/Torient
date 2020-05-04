package app.shynline.torient.utils

fun Long.toStandardRate(): String {
    var u = "B/s"
    var sr = this.toFloat()
    if (sr > 1024) {
        sr /= 1024
        u = "KB/s"
    }
    if (sr > 1024) {
        sr /= 1024
        u = "MB/s"
    }
    if (sr > 1024) {
        sr /= 1024
        u = "GB/s"
    }
    return String.format("%.2f $u", sr)
}

fun Int.toStandardRate(): String {
    return this.toLong().toStandardRate()
}