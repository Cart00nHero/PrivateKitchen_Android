package com.cartoonhero.privatekitchen_android.actors

import com.cartoonhero.theatre.Actor
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import java.text.SimpleDateFormat
import java.util.*

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class TimeGuardian: Actor() {
    suspend fun beTimeStampToText(
        datePattern: String = "yyyy/MM/dd HH:mm",
        stamp: Long, locale: Locale
    ): String {
        val actorJob = CompletableDeferred<String>()
        tell {
            val simpleDateFormat = SimpleDateFormat(datePattern, locale)
            val dateText: String = simpleDateFormat.format(Date(stamp))
            actorJob.complete(dateText)
        }
        return actorJob.await()
    }
    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    suspend fun beTextToTimeStamp(
        datePattern: String = "yyyy/MM/dd HH:mm",
        dateText: String, locale: Locale
    ): Long {
        val actorJob = CompletableDeferred<Long>()
        tell {
            val simpleDateFormat = SimpleDateFormat(datePattern, locale)
            val stamp: Long = simpleDateFormat.parse(dateText).time
            actorJob.complete(stamp)
        }
        return actorJob.await()
    }
    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    suspend fun beTextTo(
        dateText: String,
        dateFormat: String = "yyyy/MM/dd HH:mm",
        timeZone: TimeZone = TimeZone.getTimeZone("UTC")
    ): Date {
        val actorJob = CompletableDeferred<Date>()
        tell {
            val parser = SimpleDateFormat(dateFormat, Locale.getDefault())
            parser.timeZone = timeZone
            val date: Date =  parser.parse(dateText)
            actorJob.complete(date)
        }
        return actorJob.await()
    }

    suspend fun beDateToText(
        datePattern: String = "yyyy/MM/dd HH:mm",
        timeZone: TimeZone = TimeZone.getDefault(),
        date: Date
    ): String {
        val actorJob = CompletableDeferred<String>()
        tell {
            val formatter = SimpleDateFormat(datePattern, Locale.getDefault())
            formatter.timeZone = timeZone
            val dateText: String = formatter.format(date)
            actorJob.complete(dateText)
        }
        return actorJob.await()
    }
}