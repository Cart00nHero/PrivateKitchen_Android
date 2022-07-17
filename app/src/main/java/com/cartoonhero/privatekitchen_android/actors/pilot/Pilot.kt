package com.cartoonhero.privatekitchen_android.actors.pilot

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.os.IBinder
import androidx.core.app.ActivityCompat
import com.cartoonhero.theatre.Actor
import kotlinx.coroutines.*

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class Pilot constructor(private val context: Context) : Actor() {
    private lateinit var compass: Compass
    private lateinit var pilotListener: PilotInterface

    private fun actComeOn(listener: PilotInterface) {
        pilotListener = listener
        val intent = Intent()
        intent.setClass(context, Compass::class.java)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun actCheckPermission() {
        pilotListener.didPermitted(compass.checkPermission())
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
        compass.requestLocationUpdates(minTime, minDistance)
    }

    private fun actStepDown() {
        compass.removeListener(compassListener)
        context.unbindService(connection)
    }


    /** ----------------------------------------------------------------------------------------------------- **/

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder: Compass.ServiceBinder = service as Compass.ServiceBinder
            compass = binder.compass
            compass.setListener(compassListener)
            pilotListener.onCompassConnected()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            pilotListener.onCompassDisconnected()
        }

    }

    private val compassListener: CompassListener = object : CompassListener {
        override fun didUpdateLocation(location: Location) {
            pilotListener.didUpdateLocation(location)
        }
    }

    /** ----------------------------------------------------------------------------------------------------- **/

    fun beComeOn(listener: PilotInterface) {
        tell { actComeOn(listener) }
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

    fun beStepDown() {
        tell { actStepDown() }
    }
}