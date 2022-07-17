package com.cartoonhero.privatekitchen_android.stage.scene.map

import android.location.Location
import com.cartoonhero.privatekitchen_android.actors.archmage.Teleporter
import com.cartoonhero.privatekitchen_android.props.obEntities.ObAddress
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Marker

interface MapDirector {
    fun beShowTime(teleport: Teleporter)
    fun beNewLocation()
    fun beRegionChanged(newRegion: CameraPosition)
    fun beSearchKitchensHere(marker: Marker)
    fun beLowerCurtain()
}

interface FindLocDirector {
    fun beShowTime()
    fun beCollectParcels(complete:(ObAddress) -> Unit)
    fun beCurrentLocation()
    fun beInquireLocationsAddresses(locations: List<Location>)
    fun beInquireAddressesLocation(address: String)
    fun beSendAddress(complete: (() -> Unit)?)
    fun beCancelKitchenParcel(complete: (() -> Unit)?)
    fun beLowerCurtain()
}

interface TravelTimeDirector {
    fun beShowTime()
}