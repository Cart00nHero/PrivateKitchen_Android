package com.cartoonhero.privatekitchen_android.props.inlineTools

import java.text.SimpleDateFormat
import java.util.*

fun Long.toDateText(format: String = "yyyy/MM/dd HH:mm",locale: Locale): String {
    val simpleDateFormat = SimpleDateFormat(format, locale)
    return simpleDateFormat.format(Date(this))
}
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
fun String.toTimeStamp(dateFormat: String = "yyyy/MM/dd HH:mm", locale: Locale): Long {
    val simpleDateFormat = SimpleDateFormat(dateFormat, locale)
    return simpleDateFormat.parse(this).time
}
@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
fun String.toDate(
    format: String = "yyyy/MM/dd HH:mm",
    timeZone: TimeZone = TimeZone.getTimeZone("UTC")): Date {
    val parser = SimpleDateFormat(format, Locale.getDefault())
    parser.timeZone = timeZone
    return parser.parse(this)
}
fun Date.formatTo(format: String, timeZone: TimeZone = TimeZone.getDefault()): String {
    val formatter = SimpleDateFormat(format, Locale.getDefault())
    formatter.timeZone = timeZone
    return formatter.format(this)
}