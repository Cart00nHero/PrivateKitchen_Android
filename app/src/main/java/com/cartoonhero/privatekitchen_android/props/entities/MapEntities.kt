package com.cartoonhero.privatekitchen_android.props.entities

import com.google.android.gms.maps.model.LatLng

data class PlaceMarker(
    val name: String,
    val coordinate: LatLng,
    val idx: Int,
    val addressText: String
)