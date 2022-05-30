package com.cartoonhero.privatekitchen_android.stage.scenarios

import android.content.Context
import com.cartoonhero.privatekitchen_android.actors.generator.Generator
import com.cartoonhero.privatekitchen_android.actors.objBox.ObDb
import com.cartoonhero.privatekitchen_android.props.obEntities.ObWorkstation
import com.cartoonhero.privatekitchen_android.stage.scene.main.MainDirector
import com.cartoonhero.theatre.Scenario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class MainScenario: Scenario(), MainDirector {
    private fun doInitDb(context: Context) {
        launch {
            ObDb().beDebut(context)
        }
    }

    override fun beInitDb(context: Context) {
        tell {
            doInitDb(context)
        }
    }
}