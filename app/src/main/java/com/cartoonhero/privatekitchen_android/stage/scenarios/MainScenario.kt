package com.cartoonhero.privatekitchen_android.stage.scenarios

import com.cartoonhero.privatekitchen_android.actors.archmage.Archmage
import com.cartoonhero.privatekitchen_android.actors.archmage.LiveScene
import com.cartoonhero.privatekitchen_android.actors.archmage.*
import com.cartoonhero.privatekitchen_android.actors.objBox.ObDb
import com.cartoonhero.privatekitchen_android.stage.scene.main.MainDirector
import com.cartoonhero.theatre.Scenario
import kotlinx.coroutines.*

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class MainScenario: Scenario(), MainDirector {
    private val archmage: Archmage by lazy {
        Archmage(this)
    }
    private fun actInitDb() {
        launch {
            ObDb().beDebut()
        }
    }
    private fun actTestMethod() {
    }
    private fun actTestLiveData(complete: (String) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            complete("幹咧")
        }
    }

    override fun showTime() {
    }

    override fun beInitDb() {
        tell {
            actInitDb()
        }
    }

    override fun beTestMethod() {
        tell {
            actTestMethod()
        }
    }

    override fun beTestLiveData(complete: (String) -> Unit) {
        tell {
            actTestLiveData(complete)
        }
    }

    override fun beTestRedux(portal: Teleporter) {
        tell {
            archmage.beSetWaypoint(portal)
            val spell = LiveScene(prop = "Test")
            archmage.beChant(spell)
        }
    }
}