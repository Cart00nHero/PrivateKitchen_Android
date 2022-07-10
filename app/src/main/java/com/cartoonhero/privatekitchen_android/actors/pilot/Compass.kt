package com.cartoonhero.privatekitchen_android.actors.pilot

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Compass {
    private lateinit var locationManager: LocationManager
    private val pilotListeners: HashSet<PilotInterface> = hashSetOf()

    fun createManager(context: Context) {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    fun setListener(listener: PilotInterface) {
        if (!pilotListeners.contains(listener)) {
            pilotListeners.add(listener)
        }
    }
    private fun checkPermission(context: Context): Boolean {
        var permitted = true
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permitted = false
        }
        CoroutineScope(Dispatchers.Default).launch {
            for (listener in pilotListeners) {
                listener.didPermitted(permitted)
            }
        }
        return permitted
    }

    fun requestLocationUpdates(context: Context, minTime: Long, minDistance: Float) {
        if (!checkPermission(context)) return
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
                        CoroutineScope(Dispatchers.Default).launch {
                            for (listener in pilotListeners) {
                                listener.didUpdateLocation(it)
                            }
                        }
                    }
                }
                network -> mainScope.launch {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, minTime, minDistance
                    ) {
                        CoroutineScope(Dispatchers.Default).launch {
                            for (listener in pilotListeners) {
                                listener.didUpdateLocation(it)
                            }
                        }
                    }
                }
            }
        }
    }
}