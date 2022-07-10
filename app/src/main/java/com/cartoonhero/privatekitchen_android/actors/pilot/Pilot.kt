package com.cartoonhero.privatekitchen_android.actors.pilot

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import com.cartoonhero.theatre.Actor
import kotlinx.coroutines.*

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class Pilot constructor(private val context: Context) : Actor() {
    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private fun actCheckPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return true
    }

    private fun actRequestPermission(activity: Activity) {
        CoroutineScope(Dispatchers.Main).launch {
            val permissionId = 1000
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                permissionId
            )
        }
    }

    /** ----------------------------------------------------------------------------------------------------- **/

    suspend fun beCheckPermission(): Boolean {
        val actorJob = CompletableDeferred<Boolean>()
        tell {
            actorJob.complete(actCheckPermission())
        }
        return actorJob.await()
    }

    fun beRequestPermission(activity: Activity) {
        tell { actRequestPermission(activity) }
    }
}