package com.cartoonhero.privatekitchen_android.actors.mathematician

import android.location.Location
import com.cartoonhero.theatre.Actor
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class Mathematician: Actor() {
    suspend fun beHaversine(location: Location, range: Float): Boundary {
        val actorJob = CompletableDeferred<Boundary>()
        tell {
            val result = Haversine().calculateRange(location,range)
            actorJob.complete(result)
        }
        return actorJob.await()
    }
    suspend fun beCalculateDistance(from: Location, to: Location): Float {
        val actorJob = CompletableDeferred<Float>()
        tell {
            val distance = from.distanceTo(to)
            actorJob.complete(distance)
        }
        return actorJob.await()
    }
}