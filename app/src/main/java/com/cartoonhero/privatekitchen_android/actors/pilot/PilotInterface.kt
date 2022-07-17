package com.cartoonhero.privatekitchen_android.actors.pilot

import android.location.Location

interface PilotInterface {
    fun onCompassConnected()
    fun onCompassDisconnected()
    fun didPermitted(permitted: Boolean)
    fun didUpdateLocation(location: Location)
}

interface CompassListener {
    fun didUpdateLocation(location: Location)
}