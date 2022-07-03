package com.cartoonhero.privatekitchen_android.actors.pilot

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import com.cartoonhero.theatre.Actor
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import java.util.*

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class GeoCoder constructor(context: Context) : Actor() {
    private val mContext = context

    private fun actGetAddressFrom(addressText: String): Address? {
        val geoCoder = Geocoder(mContext, Locale.getDefault())
        val results = geoCoder.getFromLocationName(addressText, 1)
        return if (results.isNotEmpty()) {
            val result: Address = results.first()
            result
        } else {
            null
        }
    }

    private fun actGetAddressFrom(location: Location, locale: Locale): Address? {
        val geoCoder = Geocoder(mContext, locale)
        val results = geoCoder.getFromLocation(
            location.latitude, location.longitude, 1
        )
        return if (results.isNotEmpty()) {
            val result: Address = results.first()
            result
        } else {
            null
        }
    }

    /** ----------------------------------------------------------------------------------------------------- **/

    suspend fun beGetAddressFrom(addressText: String): Address? {
        val actorJob = CompletableDeferred<Address?>()
        tell {
            actorJob.complete(actGetAddressFrom(addressText))
        }
        return actorJob.await()
    }

    suspend fun beGetAddressFrom(location: Location, locale: Locale): Address? {
        val actorJob = CompletableDeferred<Address?>()
        tell {
            actorJob.complete(actGetAddressFrom(location, locale))
        }
        return actorJob.await()
    }
}