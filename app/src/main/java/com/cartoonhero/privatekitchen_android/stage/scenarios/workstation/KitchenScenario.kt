package com.cartoonhero.privatekitchen_android.stage.scenarios.workstation

import com.cartoonhero.privatekitchen_android.props.obEntities.ObWorkstation
import com.cartoonhero.privatekitchen_android.stage.scene.workstation.KitchenDirector
import com.cartoonhero.theatre.Courier
import com.cartoonhero.theatre.Scenario
import kotlinx.coroutines.*

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class KitchenScenario: Scenario(), KitchenDirector {
    private fun actCollectParcels(complete: (ObWorkstation) -> Unit) {
        launch {
            val pSet = Courier(this@KitchenScenario).beClaim()
            for (parcel in pSet) {
                when (parcel.content) {
                    is ObWorkstation -> {
                        val content: ObWorkstation = parcel.content as ObWorkstation
                        withContext(Dispatchers.Main) {
                            complete(content)
                        }
                    }
                }
            }
        }
    }
    private fun actSendParcels(
        recipient: String, parcel: ObWorkstation,
        complete: (() -> Unit)?
    ) {
        launch {
            Courier(this@KitchenScenario).beApply(
                parcel, recipient
            )
            withContext(Dispatchers.Main) {
                complete?.let { it() }
            }
        }
    }
 /** -------------------------------------------------------------------------------------------------------------- **/
    override fun beCollectParcels(complete: (ObWorkstation) -> Unit) {
        actCollectParcels(complete)
    }

    override fun beSendParcels(
        recipient: String, parcel: ObWorkstation,
        complete: (() -> Unit)?
    ) {
        tell { actSendParcels(recipient, parcel, complete) }
    }

}