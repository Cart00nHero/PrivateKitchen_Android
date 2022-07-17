package com.cartoonhero.privatekitchen_android.stage.scenarios.map

import android.app.Activity
import android.location.Location
import com.apollographql.apollo3.api.Optional
import com.cartoonhero.privatekitchen_android.actors.apolloApi.ApiStatus
import com.cartoonhero.privatekitchen_android.actors.apolloApi.Icarus
import com.cartoonhero.privatekitchen_android.actors.archmage.Archmage
import com.cartoonhero.privatekitchen_android.actors.archmage.LiveScene
import com.cartoonhero.privatekitchen_android.actors.archmage.Spell
import com.cartoonhero.privatekitchen_android.actors.archmage.Teleporter
import com.cartoonhero.privatekitchen_android.actors.mathematician.Mathematician
import com.cartoonhero.privatekitchen_android.actors.pilot.Pilot
import com.cartoonhero.privatekitchen_android.actors.pilot.PilotInterface
import com.cartoonhero.privatekitchen_android.stage.scene.map.MapDirector
import com.cartoonhero.theatre.Scenario
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import graphqlApollo.client.type.Coordinate
import graphqlApollo.client.type.QueryAddress
import kotlinx.coroutines.*
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.abs

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class MapScenario(private val activity: Activity) : Scenario(), MapDirector {
    private data class CenterPoint(
        var coordinate: LatLng = LatLng(0.0,0.0),
        var zoomLevel: Float = 0.5F
    )
    private data class PtMovement(
        var distance: Float = 0.0F,
        var zoomLevel: Float = 0.5F
    )
    private val pilot: Pilot by lazy {
        Pilot(activity)
    }
    private val archmage: Archmage by lazy {
        Archmage(this)
    }
    private var centerPt = CenterPoint()
    private var timerTask: TimerTask? = null

    private fun actShowTime(teleport: Teleporter) {
        archmage.beSetWaypoint(teleport)
        archmage.beSetTeleportation(teleportation)
        pilot.beComeOn(pilotListener)
    }

    private fun actNewLocation() {
        pilot.beRequestLocationUpdates(1000, 0.1F)
    }

    private fun actRegionChanged(newRegion: CameraPosition) {
        val mathJob: Deferred<PtMovement> = async {
            val movement = PtMovement()
            val results = FloatArray(1)
            Location.distanceBetween(
                centerPt.coordinate.latitude, centerPt.coordinate.longitude,
                newRegion.target.latitude, newRegion.target.longitude, results
            )
            movement.distance = abs(results.first())
            movement.zoomLevel = newRegion.zoom
            movement
        }
        if (timerTask != null) return
        timerTask = Timer().schedule(1000) {
            launch {
                val movement = mathJob.await()
                timerTask = null
                if (centerPt.zoomLevel != movement.zoomLevel) {
                    val zoomLv = movement.zoomLevel
                    centerPt.zoomLevel = zoomLv
                    if (zoomLv < 18 && zoomLv > 14) {
                        searchNearBy()
                    }
                    return@launch
                }
                val diameter = 2 * searchRange(centerPt.zoomLevel.toInt()) * 1000.0
                if (movement.distance > diameter) {
                    centerPt.coordinate = newRegion.target
                    searchNearBy()
                }
            }
        }
    }

    private fun actSearchKitchensHere(marker: Marker) {
        val addressQuery = QueryAddress(
            completion = Optional.presentIfNotNull(marker.snippet)
        )
        Icarus(this).beSearchMatchedAddress(addressQuery) {status, respData ->
            when(status) {
                ApiStatus.SUCCESS -> {
                    if (respData != null) {
                        print("parse data")
                    }
                }
                ApiStatus.FAILED -> {
                    print("parse data")
                }
            }
        }
    }

    private fun actLowerCurtain() {
        archmage.beShutOff()
        pilot.beStepDown()
    }

    private fun pilotDidPermitted(permitted: Boolean) {
        if (permitted) {
            actNewLocation()
        } else {
            pilot.beRequestPermission(activity)
        }
    }

    private fun pilotDidUpdateLocation(location: Location) {
        centerPt.coordinate = LatLng(location.latitude,location.longitude)
        archmage.beChant(LiveScene(prop = location))
    }

    /** ----------------------------------------------------------------------------------------------------- **/

    private fun mapDiameter(zoomLevel: Int): Double {
        when {
            zoomLevel > 16 ->  return 400.0
            zoomLevel == 16 ->  return 1000.0
            zoomLevel < 16 -> return 2000.0
        }
        return 1000.0
    }
    private fun searchRange(zoomLevel: Int): Double {
        // KM
        if (zoomLevel > 16) return 0.2
        if (zoomLevel == 16) return 0.5
        if (zoomLevel < 16) return 1.0
        return 1.0
    }

    private fun searchNearBy() {
        launch {
            val range = mapDiameter(centerPt.zoomLevel.toInt())
            val boundary = Mathematician().beHaversine(centerPt.coordinate, range)
            val max = Coordinate(latitude = boundary.maxLat, longitude = boundary.maxLng)
            val min = Coordinate(latitude = boundary.minLat, longitude = boundary.minLng)
            Icarus(this@MapScenario).beSearchNearby(max,min) { status, _ ->
                when(status) {
                    ApiStatus.SUCCESS -> launch {
                        // TODO: Convert to map
                    }
                    ApiStatus.FAILED -> print("Test")
                }
            }
        }
    }
    private val pilotListener: PilotInterface = object : PilotInterface {
        override fun onCompassConnected() {
            tell { pilot.beCheckPermission() }
        }

        override fun onCompassDisconnected() {
        }

        override fun didPermitted(permitted: Boolean) {
            tell { pilotDidPermitted(permitted) }
        }

        override fun didUpdateLocation(location: Location) {
            tell { pilotDidUpdateLocation(location) }
        }

    }

    private val teleportation: Teleporter = object : Teleporter {
        override fun beSpellCraft(spell: Spell) {
        }
    }

    /** ----------------------------------------------------------------------------------------------------- **/

    override fun beShowTime(teleport: Teleporter) {
        tell { actShowTime(teleport) }
    }

    override fun beNewLocation() {
        tell { actNewLocation() }
    }

    override fun beRegionChanged(newRegion: CameraPosition) {
        tell { actRegionChanged(newRegion) }
    }

    override fun beSearchKitchensHere(marker: Marker) {
        tell { actSearchKitchensHere(marker) }
    }

    override fun beLowerCurtain() {
        tell { actLowerCurtain() }
    }


}