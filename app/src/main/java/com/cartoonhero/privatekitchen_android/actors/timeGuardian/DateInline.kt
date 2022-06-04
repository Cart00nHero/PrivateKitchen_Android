package com.cartoonhero.privatekitchen_android.actors.timeGuardian

import java.text.SimpleDateFormat
import java.util.*

const val dateFormat1 = "EEEE, MMM d, yyyy"
const val dateFormat2 = "MM/dd/yyyy"
const val dateFormat3 = "MM-dd-yyyy HH:mm"
const val dateFormat4 = "MMM d, h:mm a"
const val dateFormat5 = "MMMM yyyy"
const val dateFormat6 = "MMM d, yyyy"
const val dateFormat7 = "E, d MMM yyyy HH:mm:ss Z"
const val dateFormat9 = "dd.MM.yy"
const val formatHmsS = "HH:mm:ss.SSS"
const val formatHm = "HH:mm"
const val formatYMdHm = "yyyy/MM/dd HH:mm"
const val format_yMdHm = "yyyy-MM-dd HH:mm"
const val formatISO8601 = "yyyy-MM-dd'T'HH:mm:ssZ"


fun String.toDate(
    format: String = formatYMdHm,
    timeZone: TimeZone = TimeZone.getTimeZone("UTC")
): Date {
    val parser = SimpleDateFormat(format, Locale.getDefault())
    parser.timeZone = timeZone
    return parser.parse(this) ?: Date()
}
fun Date.toText(format: String, timeZone: TimeZone = TimeZone.getDefault()): String {
    val formatter = SimpleDateFormat(format, Locale.getDefault())
    formatter.timeZone = timeZone
    return formatter.format(this)
}
