package com.cartoonhero.privatekitchen_android.actors.mathematician

import android.location.Location
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
    fun calculateRange(location:Location, range: Float): Boundary {
        val dLng = dLongitude(location,range)
        val dLat = dLatitude(location, range)
        val maxLat = location.latitude + dLat
        val minLat = location.latitude - dLat
        val maxLng = location.longitude + dLng
        val minLng = location.longitude - dLng

        return Boundary(maxLat,maxLng,minLat,minLng)
    }
    private fun dLongitude(location:Location, range: Float): Double {
        val dLng =
            2 * asin(sin(range/(2*earthRadius)/ cos(deg2rad(location.latitude))))
        return rad2deg(dLng)
    }
    private fun dLatitude(location:Location, range: Float): Double {
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