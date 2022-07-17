package com.cartoonhero.privatekitchen_android.actors.pilot

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Binder
import android.os.IBinder
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Compass : Service() {
    private val locationManager =
        this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val compassListeners: HashSet<CompassListener> = hashSetOf()

    inner class ServiceBinder : Binder() {
        val compass: Compass get() = this@Compass
    }

    override fun onBind(intent: Intent): IBinder {
        return ServiceBinder()
    }

    fun setListener(listener: CompassListener) {
        if (!compassListeners.contains(listener)) {
            compassListeners.add(listener)
        }
    }

    fun removeListener(listener: CompassListener) {
        compassListeners.remove(listener)
    }

    fun checkPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return true
    }

    fun requestLocationUpdates(minTime: Long, minDistance: Float) {
        if (!checkPermission()) return
        val gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (gps || network) {
            val mainScope = CoroutineScope(Dispatchers.Main)
            when {
                // fix:
                // Can't create handler inside thread Thread[DefaultDispatcher-worker-2,5,main]
                // that has not called Looper.prepare()
                gps -> mainScope.launch {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, minTime, minDistance
                    ) {
                        compassDidUpdated(it)
                    }
                }
                network -> mainScope.launch {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, minTime, minDistance
                    ) {
                        compassDidUpdated(it)
                    }
                }
            }
        }
    }

    private fun compassDidUpdated(location: Location) {
        for (listener in compassListeners) {
            listener.didUpdateLocation(location)
        }
    }
}