package com.cartoonhero.privatekitchen_android.stage.scenarios.map

import android.app.Activity
import android.location.Location
import com.cartoonhero.privatekitchen_android.actors.archmage.Archmage
import com.cartoonhero.privatekitchen_android.actors.archmage.LiveScene
import com.cartoonhero.privatekitchen_android.actors.archmage.Spell
import com.cartoonhero.privatekitchen_android.actors.archmage.Teleporter
import com.cartoonhero.privatekitchen_android.actors.pilot.Pilot
import com.cartoonhero.privatekitchen_android.actors.pilot.PilotInterface
import com.cartoonhero.privatekitchen_android.stage.scene.map.MapDirector
import com.cartoonhero.theatre.Scenario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class MapScenario(private val activity: Activity) : Scenario(), MapDirector {
    private val pilot: Pilot by lazy {
        Pilot(activity)
    }
    private val archmage: Archmage by lazy {
        Archmage(this)
    }

    private fun actShowTime(teleport: Teleporter) {
        archmage.beSetWaypoint(teleport)
        archmage.beSetTeleportation(teleportation)
        pilot.beComeOn(pilotListener)
    }

    private fun actNewLocation() {
        pilot.beRequestLocationUpdates(1000, 0.1F)
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
        archmage.beChant(LiveScene(prop = location))
    }

    /** ----------------------------------------------------------------------------------------------------- **/

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

    override fun beLowerCurtain() {
        tell { actLowerCurtain() }
    }


}