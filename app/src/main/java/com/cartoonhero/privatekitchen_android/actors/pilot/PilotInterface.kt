package com.cartoonhero.privatekitchen_android.actors.pilot

import android.location.Location

interface PilotInterface {
    fun didPermitted(permitted: Boolean)
    fun didUpdateLocation(location: Location)
}