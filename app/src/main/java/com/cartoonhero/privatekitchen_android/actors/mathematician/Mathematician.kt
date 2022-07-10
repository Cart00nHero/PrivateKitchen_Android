package com.cartoonhero.privatekitchen_android.actors.mathematician

import android.location.Location
import com.cartoonhero.theatre.Actor
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class Mathematician : Actor() {

    private fun actHaversine(location: Location, range: Float): Boundary {
        return Haversine().calculateRange(location, range)
    }

    /** ----------------------------------------------------------------------------------------------------- **/

    suspend fun beHaversine(location: Location, range: Float): Boundary {
        val actorJob = CompletableDeferred<Boundary>()
        tell {
            actorJob.complete(actHaversine(location, range))
        }
        return actorJob.await()
    }

    suspend fun beCalculateDistance(from: Location, to: Location): Float {
        val actorJob = CompletableDeferred<Float>()
        tell {
            actorJob.complete(from.distanceTo(to))
        }
        return actorJob.await()
    }
}