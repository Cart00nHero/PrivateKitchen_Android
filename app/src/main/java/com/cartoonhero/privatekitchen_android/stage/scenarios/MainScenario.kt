package com.cartoonhero.privatekitchen_android.stage.scenarios

import android.content.Context
import com.cartoonhero.privatekitchen_android.actors.objBox.ObDb
import com.cartoonhero.privatekitchen_android.stage.scene.main.MainDirector
import com.cartoonhero.theatre.Scenario
import kotlinx.coroutines.*

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class MainScenario: Scenario(), MainDirector {
    private fun actInitDb(context: Context) {
        launch {
            ObDb().beDebut(context)
        }
    }

    override fun beInitDb(context: Context) {
        tell {
            actInitDb(context)
        }
    }
}