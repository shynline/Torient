package app.shynline.torient.torrent.utils

fun gcd(_a: Int, _b: Int): Int {
    var a = _a
    var b = _b
    var temp: Int
    while (b > 0) {
        temp = b
        b = a % b // % is remainder
        a = temp
    }
    return a
}

fun gcd(input: List<Int>): Int {
    var result = input[0]
    for (i in 1 until input.size) {
        result = gcd(result, input[i])
        if (result == 1)
            break
    }
    return result
}