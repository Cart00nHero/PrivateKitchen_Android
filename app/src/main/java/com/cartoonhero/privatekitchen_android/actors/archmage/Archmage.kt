package com.cartoonhero.privatekitchen_android.actors.archmage

import com.cartoonhero.theatre.Actor
import com.cartoonhero.theatre.Scenario
import kotlinx.coroutines.*
import org.reduxkotlin.createThreadSafeStore

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class Archmage(private val destination: Scenario) : Actor() {
    private var teleportations: HashSet<Teleporter> = hashSetOf()
    private var wayPoints: HashSet<Teleporter> = hashSetOf()
    private val wand = Teleportation.portal
    private val unsubscribe = wand.subscribe { newState(wand.state) }

    private fun newState(state: AppState) {
        destination.tell {
            for (teleportation in teleportations) {
                teleportation.beSpellCraft(state.spell)
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            for (point in wayPoints) {
                point.beSpellCraft(state.spell)
            }
        }
    }

    private fun actSetTeleportation(teleporter: Teleporter) {
        teleportations.clear()
        teleportations.add(teleporter)
    }

    private fun actSetWaypoint(wayPoint: Teleporter) {
        wayPoints.clear()
        wayPoints.add(wayPoint)
    }

    private fun actChant(spell: Spell) {
        wand.dispatch(spell)
    }

    private fun actShutOff() {
        unsubscribe()
        teleportations.clear()
        wayPoints.clear()
    }

    /** ----------------------------------------------------------------------------------------------------- **/

    fun beSetTeleportation(teleporter: Teleporter) {
        tell { actSetTeleportation(teleporter) }
    }

    fun beSetWaypoint(wayPoint: Teleporter) {
        tell { actSetWaypoint(wayPoint) }
    }

    fun beChant(spell: Spell) {
        tell { actChant(spell) }
    }

    fun beShutOff() {
        tell { actShutOff() }
    }

    private object Teleportation {
        val portal = createThreadSafeStore(::mageReducer, AppState())
    }
}
