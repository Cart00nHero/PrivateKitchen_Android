package com.cartoonhero.privatekitchen_android.actors.mathematician

import android.location.Location
import com.cartoonhero.theatre.Actor
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class Mathematician : Actor() {

    private fun actHaversine(center: LatLng, range: Double): Boundary {
        return Haversine().calculateRange(center, range)
    }

    /** ----------------------------------------------------------------------------------------------------- **/

    suspend fun beHaversine(center: LatLng, range: Double): Boundary {
        val actorJob = CompletableDeferred<Boundary>()
        tell {
            actorJob.complete(actHaversine(center, range))
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