package com.cartoonhero.privatekitchen_android.actors.archmage

import com.cartoonhero.theatre.Actor
import com.cartoonhero.theatre.Scenario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.reduxkotlin.createThreadSafeStore

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class Archmage(private val destination: Scenario): Actor() {
    lateinit var teleporter: Teleporter
    private val wand = Teleportation.portal
    private val unsubscribe = wand.subscribe{ newState(wand.state) }

    private fun newState(state: AppState) {
        destination.tell {
            teleporter.beNewState(state)
        }
    }
    fun beSubscribe(teleporter: Teleporter) {
        tell {
            this.teleporter = teleporter
        }
    }
    fun beChant(spell: Spell) {
        tell {
            wand.dispatch(spell)
        }
    }
    fun beUnsubscribe() {
        tell {
            unsubscribe()
        }
    }

    private object Teleportation {
        val portal = createThreadSafeStore(::mageReducer, AppState())
        fun subscribe() {
        }
        fun unsubscribe() {
        }
    }
}
