package com.cartoonhero.privatekitchen_android.stage.scenarios

import android.content.Context
import com.apollographql.apollo3.api.Optional
import com.cartoonhero.privatekitchen_android.actors.Transformer
import com.cartoonhero.privatekitchen_android.actors.objBox.ObDb
import com.cartoonhero.privatekitchen_android.stage.scene.main.MainDirector
import com.cartoonhero.theatre.Scenario
import graphqlApollo.operation.type.InputOpAddress
import kotlinx.coroutines.*

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class MainScenario: Scenario(), MainDirector {
    private fun actInitDb() {
        launch {
            ObDb().beDebut()
        }
    }
    private fun actTestMethod() {
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
}