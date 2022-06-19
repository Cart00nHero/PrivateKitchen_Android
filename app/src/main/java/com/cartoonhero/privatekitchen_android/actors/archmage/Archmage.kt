package com.cartoonhero.privatekitchen_android.actors.archmage

import com.cartoonhero.theatre.Actor
import com.cartoonhero.theatre.Scenario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.reduxkotlin.createThreadSafeStore

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class Archmage(private val destination: Scenario): Actor() {
    private var wayPoints: HashSet<Teleporter> = hashSetOf()
    private val wand = Teleportation.portal
    private val unsubscribe = wand.subscribe{ newState(wand.state) }

    private fun newState(state: AppState) {
        destination.tell {
            for (point in wayPoints) {
                point.beNewState(state)
            }
        }
    }
    fun beSetWaypoint(teleporter: Teleporter) {
        tell {
            if (!wayPoints.contains(teleporter)) {
                this.wayPoints.add(teleporter)
            }
        }
    }
    fun beChant(spell: Spell) {
        tell {
            wand.dispatch(spell)
        }
    }
    fun beShutOffPoint(teleporter: Teleporter) {
        tell {
            wayPoints.remove(teleporter)
        }
    }
    fun beShutOff() {
        tell {
            wayPoints.clear()
            unsubscribe()
        }
    }

    private object Teleportation {
        val portal = createThreadSafeStore(::mageReducer, AppState())
    }
}
