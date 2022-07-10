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
    private lateinit var pilotListener: PilotInterface

    private fun actSetListener(listener: PilotInterface) {
        pilotListener = listener
    }

    private fun actCheckPermission() {
        pilotListener.didPermitted(checkPermission())
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

    private fun actRequestLocationUpdates(minTime: Long, minDistance: Float) {
        if (!checkPermission()) return
        val gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (gps || network) {
            val mainScope = CoroutineScope(Dispatchers.Main)
            when {
                gps -> mainScope.launch {
                    // fix:
                    // Can't create handler inside thread Thread[DefaultDispatcher-worker-2,5,main]
                    // that has not called Looper.prepare()
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, minTime, minDistance
                    ) {
                        pilotListener.didUpdateLocation(it)
                    }
                }
                network -> mainScope.launch {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, minTime, minDistance
                    ) {
                        pilotListener.didUpdateLocation(it)
                    }
                }
            }
        }
    }


    /** ----------------------------------------------------------------------------------------------------- **/

    private fun checkPermission(): Boolean {
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

    /** ----------------------------------------------------------------------------------------------------- **/

    fun beSetListener(listener: PilotInterface) {
        tell { actSetListener(listener) }
    }

    fun beCheckPermission() {
        tell { actCheckPermission() }
    }

    fun beRequestPermission(activity: Activity) {
        tell { actRequestPermission(activity) }
    }

    fun beRequestLocationUpdates(minTime: Long, minDistance: Float) {
        tell { actRequestLocationUpdates(minTime, minDistance) }
    }
}