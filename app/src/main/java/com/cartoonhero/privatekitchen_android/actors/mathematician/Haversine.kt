package com.cartoonhero.privatekitchen_android.actors.mathematician

import com.google.android.gms.maps.model.LatLng
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin

data class Boundary(
    val maxLat: Double,
    val maxLng: Double,
    val minLat: Double,
    val minLng: Double
)
class Haversine {
    private val earthRadius = 6371.0
    fun calculateRange(center: LatLng, range: Double): Boundary {
        val dLng = dLongitude(center,range)
        val dLat = dLatitude(range)
        val maxLat = center.latitude + dLat
        val minLat = center.latitude - dLat
        val maxLng = center.longitude + dLng
        val minLng = center.longitude - dLng

        return Boundary(maxLat,maxLng,minLat,minLng)
    }
    private fun dLongitude(center: LatLng, range: Double): Double {
        val dLng =
            2 * asin(sin(range/(2 * earthRadius)/ cos(deg2rad(center.latitude))))
        return rad2deg(dLng)
    }
    private fun dLatitude(range: Double): Double {
        val dLat = range / earthRadius
        return rad2deg(dLat)
    }
    private fun deg2rad(number: Double): Double {
        return number * PI / 180.0
    }
    private fun rad2deg(number: Double): Double {
        return number * 180.0 / PI
    }
}