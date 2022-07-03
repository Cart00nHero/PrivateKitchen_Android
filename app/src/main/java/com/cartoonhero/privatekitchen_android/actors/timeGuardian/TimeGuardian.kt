package com.cartoonhero.privatekitchen_android.actors.timeGuardian

import com.cartoonhero.theatre.Actor
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class TimeGuardian : Actor() {

    private fun actISO8601(dateText: String): Date {
        val formatter = SimpleDateFormat(formatISO8601, Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.parse(dateText) ?: Date()
    }

    private fun actISO8601(date: Date): String {
        val formatter = SimpleDateFormat(formatISO8601, Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(date)
    }

    private fun actTimeStampTo(
        stamp: Long,
        format: String = formatYMdHm,
        locale: Locale = Locale.getDefault()
    ): String {
        val dateFormat = SimpleDateFormat(format, locale)
        return dateFormat.format(Date(stamp))
    }

    private fun actTextTo(
        dateText: String,
        format: String = formatYMdHm,
        locale: Locale
    ): Long {
        val dateFormat = SimpleDateFormat(format, locale)
        return dateFormat.parse(dateText)?.time ?: 0
    }

    private fun actStraightTo(
        dateText: String,
        format: String = formatYMdHm,
    ): Date {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        return dateFormat.parse(dateText) ?: Date()
    }

    private fun actStraightTo(date: Date, format: String): String {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        return dateFormat.format(date)
    }

    private fun actDateTo(
        date: Date, format: String,
        timeZone: TimeZone
    ): Date {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        val dateText: String = dateFormat.format(date)
        dateFormat.timeZone = timeZone
        return dateFormat.parse(dateText) ?: Date()
    }

    private fun actTextTo(
        dateText: String, format: String,
        timeZone: TimeZone
    ): Date {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        dateFormat.timeZone = timeZone
        return dateFormat.parse(dateText) ?: Date()
    }

    private fun actDateToText(
        date: Date, format: String,
        fromTZ: TimeZone, toTZ: TimeZone
    ): String {
        val dateText: String = date.toText(format, fromTZ)
        val fromDate = dateText.toDate(format, fromTZ)
        return fromDate.toText(format, toTZ)
    }

    private fun actTextToText(
        dateText: String, format: String,
        fromTZ: TimeZone, toTZ: TimeZone
    ): String {
        val from: Date = dateText.toDate(format, fromTZ)
        return from.toText(format, toTZ)
    }

    private fun actSplitTime(interval: Long): TimeTuple {
        val seconds = (interval % 60).toInt()
        val minutes = ((interval / 60) % 60).toInt()
        val hours = ((interval / 3600)).toInt()
        return TimeTuple(hours, minutes, seconds)
    }

    private fun actTomorrow(): Date {
        val dateFormat = SimpleDateFormat(formatYMdHm, Locale.getDefault())
        val dateText: String = dateFormat.format(Date())
        var stamp: Long = dateFormat.parse(dateText)?.time ?: 0
        stamp += 86400
        return Date(stamp)
    }

    /** ----------------------------------------------------------------------------------------------------- **/

    suspend fun beISO8601(dateText: String): Date {
        val actorJob = CompletableDeferred<Date>()
        tell {
            actorJob.complete(actISO8601(dateText))
        }
        return actorJob.await()
    }

    suspend fun beISO8601(date: Date): String {
        val actorJob = CompletableDeferred<String>()
        tell {
            actorJob.complete(actISO8601(date))
        }
        return actorJob.await()
    }

    suspend fun beTimeStampTo(
        stamp: Long,
        format: String = formatYMdHm,
        locale: Locale = Locale.getDefault()
    ): String {
        val actorJob = CompletableDeferred<String>()
        tell {
            actorJob.complete(actTimeStampTo(stamp, format, locale))
        }
        return actorJob.await()
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    suspend fun beTextTo(
        dateText: String,
        format: String = formatYMdHm,
        locale: Locale
    ): Long {
        val actorJob = CompletableDeferred<Long>()
        tell {
            actorJob.complete(actTextTo(dateText, format, locale))
        }
        return actorJob.await()
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    suspend fun beStraightTo(
        dateText: String,
        format: String = formatYMdHm,
    ): Date {
        val actorJob = CompletableDeferred<Date>()
        tell {
            actorJob.complete(actStraightTo(dateText, format))
        }
        return actorJob.await()
    }

    suspend fun beStraightTo(date: Date, format: String): String {
        val actorJob = CompletableDeferred<String>()
        tell {
            actorJob.complete(actStraightTo(date, format))
        }
        return actorJob.await()
    }

    suspend fun beDateTo(
        date: Date, format: String,
        timeZone: TimeZone
    ): Date {
        val actorJob = CompletableDeferred<Date>()
        tell {
            actorJob.complete(actDateTo(date, format, timeZone))
        }
        return actorJob.await()
    }

    suspend fun beTextTo(
        dateText: String, format: String,
        timeZone: TimeZone
    ): Date {
        val actorJob = CompletableDeferred<Date>()
        tell {
            actorJob.complete(actTextTo(dateText, format, timeZone))
        }
        return actorJob.await()
    }

    suspend fun beDateToText(
        date: Date, format: String,
        fromTZ: TimeZone, toTZ: TimeZone
    ): String {
        val actorJob = CompletableDeferred<String>()
        tell {
            actorJob.complete(actDateToText(date, format, fromTZ, toTZ))
        }
        return actorJob.await()
    }

    suspend fun beTextToText(
        dateText: String, format: String,
        fromTZ: TimeZone, toTZ: TimeZone
    ): String {
        val actorJob = CompletableDeferred<String>()
        tell {
            actorJob.complete(actTextToText(dateText, format, fromTZ, toTZ))
        }
        return actorJob.await()
    }

    suspend fun beSplitTime(interval: Long): TimeTuple {
        val actorJob = CompletableDeferred<TimeTuple>()
        tell {
            actorJob.complete(actSplitTime(interval))
        }
        return actorJob.await()
    }

    suspend fun beTomorrow(): Date {
        val actorJob = CompletableDeferred<Date>()
        tell {
            actorJob.complete(actTomorrow())
        }
        return actorJob.await()
    }
}