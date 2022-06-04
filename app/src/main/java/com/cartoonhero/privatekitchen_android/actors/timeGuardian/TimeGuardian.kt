package com.cartoonhero.privatekitchen_android.actors.timeGuardian

import com.cartoonhero.theatre.Actor
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class TimeGuardian: Actor() {
    suspend fun beISO8601(dateText: String): Date {
        val actorJob = CompletableDeferred<Date>()
        tell {
            val formatter = SimpleDateFormat(formatISO8601, Locale.getDefault())
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            val isoDate: Date = formatter.parse(dateText) ?: Date()
            actorJob.complete(isoDate)
        }
        return actorJob.await()
    }
    suspend fun beISO8601(date: Date): String {
        val actorJob = CompletableDeferred<String>()
        tell {
            val formatter = SimpleDateFormat(formatISO8601, Locale.getDefault())
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            val dateText: String = formatter.format(date)
            actorJob.complete(dateText)
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
            val dateFormat = SimpleDateFormat(format, locale)
            val dateText: String = dateFormat.format(Date(stamp))
            actorJob.complete(dateText)
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
            val dateFormat = SimpleDateFormat(format, locale)
            val stamp: Long = dateFormat.parse(dateText).time
            actorJob.complete(stamp)
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
            val dateFormat = SimpleDateFormat(format, Locale.getDefault())
            val date: Date =  dateFormat.parse(dateText)
            actorJob.complete(date)
        }
        return actorJob.await()
    }
    suspend fun beStraightTo(date: Date, format: String): String {
        val actorJob = CompletableDeferred<String>()
        tell {
            val dateFormat = SimpleDateFormat(format, Locale.getDefault())
            val dateText: String = dateFormat.format(date)
            actorJob.complete(dateText)
        }
        return actorJob.await()
    }
    suspend fun beDateTo(
        date: Date, format: String,
        timeZone: TimeZone
    ): Date {
        val actorJob = CompletableDeferred<Date>()
        tell {
            val dateFormat = SimpleDateFormat(format, Locale.getDefault())
            val dateText: String = dateFormat.format(date)
            dateFormat.timeZone = timeZone
            val newDate: Date =  dateFormat.parse(dateText) ?: Date()
            actorJob.complete(newDate)
        }
        return actorJob.await()
    }
    suspend fun beTextTo(
        dateText: String, format: String,
        timeZone: TimeZone
    ): Date {
        val actorJob = CompletableDeferred<Date>()
        tell {
            val dateFormat = SimpleDateFormat(format, Locale.getDefault())
            dateFormat.timeZone = timeZone
            val date: Date =  dateFormat.parse(dateText) ?: Date()
            actorJob.complete(date)
        }
        return actorJob.await()
    }
    suspend fun beDateToText(
        date: Date, format: String,
        fromTZ: TimeZone, toTZ: TimeZone
    ): String {
        val actorJob = CompletableDeferred<String>()
        tell {
            val dateText: String = date.toText(format,fromTZ)
            val fromDate = dateText.toDate(format,fromTZ)
            val toDateText: String = fromDate.toText(format, toTZ)
            actorJob.complete(toDateText)
        }
        return actorJob.await()
    }
    suspend fun beTextToText(
        dateText: String, format: String,
        fromTZ: TimeZone, toTZ: TimeZone
    ): String {
        val actorJob = CompletableDeferred<String>()
        tell {
            val from: Date = dateText.toDate(format,fromTZ)
            val newText: String = from.toText(format,toTZ)
            actorJob.complete(newText)
        }
        return actorJob.await()
    }
    suspend fun beSplitTime(interval: Long): TimeTuple {
        val actorJob = CompletableDeferred<TimeTuple>()
        tell {
            val seconds = (interval % 60).toInt()
            val minutes = ((interval / 60) % 60).toInt()
            val hours = ((interval / 3600)).toInt()
            actorJob.complete(TimeTuple(hours,minutes,seconds))
        }
        return actorJob.await()
    }
    suspend fun beTomorrow(): Date {
        val actorJob = CompletableDeferred<Date>()
        tell {
            val dateFormat = SimpleDateFormat(formatYMdHm, Locale.getDefault())
            val dateText: String = dateFormat.format(Date())
            var stamp: Long = dateFormat.parse(dateText)?.time ?: 0
            stamp += 86400
            actorJob.complete(Date(stamp))
        }
        return actorJob.await()
    }
}