package app.shynline.torient.utils

fun Long.toStandardRate(): String {
    return "${this.toByteRepresentation()}/s"
}

fun Int.toStandardRate(): String {
    return this.toLong().toStandardRate()
}

fun Int.toByteRepresentation(): String {
    return this.toLong().toByteRepresentation()
}

fun Long.toByteRepresentation(): String {
    var byte = true
    var u = "B"
    var sr = this.toFloat()
    if (sr > 1024) {
        sr /= 1024
        u = "KB"
        byte = false
    }
    if (sr > 1024) {
        sr /= 1024
        u = "MB"
    }
    if (sr > 1024) {
        sr /= 1024
        u = "GB"
    }
    if (sr > 1024) {
        sr /= 1024
        u = "TB"
    }
    return if (byte) String.format("%d $u", sr.toInt()) else String.format("%.2f $u", sr)
}