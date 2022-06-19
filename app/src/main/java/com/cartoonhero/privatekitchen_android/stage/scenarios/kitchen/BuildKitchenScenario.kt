package com.cartoonhero.privatekitchen_android.stage.scenarios.kitchen

import com.apollographql.apollo3.api.Optional
import com.cartoonhero.privatekitchen_android.actors.InputChecker
import com.cartoonhero.privatekitchen_android.actors.Transformer
import com.cartoonhero.privatekitchen_android.actors.apolloApi.ApiStatus
import com.cartoonhero.privatekitchen_android.actors.apolloApi.Helios
import com.cartoonhero.privatekitchen_android.actors.objBox.ObDb
import com.cartoonhero.privatekitchen_android.patterns.Transcribe
import com.cartoonhero.privatekitchen_android.props.entities.KitchenInfo
import com.cartoonhero.privatekitchen_android.props.obEntities.ObAddress
import com.cartoonhero.privatekitchen_android.props.obEntities.ObStKitchen
import com.cartoonhero.privatekitchen_android.props.obEntities.ObWorkstation
import com.cartoonhero.privatekitchen_android.stage.scene.buildMyKitchen.BuildKitchenDirector
import com.cartoonhero.theatre.Courier
import com.cartoonhero.theatre.Scenario
import graphqlApollo.operation.type.InputStKinfo
import graphqlApollo.operation.type.InputStKitchen
import kotlinx.coroutines.*

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class BuildKitchenScenario: Scenario(), BuildKitchenDirector {
    private var wkStation: ObWorkstation? = null

    private fun actCollectParcels(complete: (ObStKitchen) -> Unit) {
        launch {
            val pSet = Courier(this@BuildKitchenScenario).beClaim()
            for (parcel in pSet) {
                when(parcel.content) {
                    is ObWorkstation -> {
                        val content: ObWorkstation = parcel.content as ObWorkstation
                        wkStation = content
                        val kitchen: ObStKitchen = content
                            .kitchen.target ?: ObStKitchen(id = 0)
                        CoroutineScope(Dispatchers.Main).launch {
                            complete(kitchen)
                        }
                    }
                }
            }
        }
    }
    private fun actCheckInput(phone: String, complete: (Boolean) -> Unit) {
        launch {
            if (phone.isNotEmpty()) {
                val passed: Boolean = InputChecker().beMobileNumber(phone)
                withContext(Dispatchers.Main) {
                    complete(passed)
                }
            } else {
                withContext(Dispatchers.Main) {
                    complete(true)
                }
            }
        }
    }
    private fun actSaveKitchen(info: KitchenInfo, floor: String, complete: (() -> Unit)?) {
        if (wkStation == null) return
        val stationJob: Deferred<ObWorkstation> = async {
            val kitchen: ObStKitchen = wkStation!!.kitchen.target
            kitchen.info = Transformer().beToJson(info)
            val newAddress: ObAddress = kitchen.address.target ?: ObAddress(id = 0)
            newAddress.floor = floor
            kitchen.address.target = newAddress
            wkStation!!.kitchen.target = kitchen
            return@async wkStation!!
        }
        val savingJob: Deferred<Boolean> = async {
            val newStation = stationJob.await()
            val objBox = ObDb().beTakeBox(ObWorkstation::class.java)
            objBox.put(newStation)
            return@async true
        }
        launch {
            if (savingJob.await()) {
                val newAddress: ObAddress = wkStation!!.kitchen.target.address.target
                val iopAddress = Transcribe(this@BuildKitchenScenario)
                    .beObAddressTo(newAddress)
                val inputInfo = InputStKinfo(
                    name = Optional.presentIfNotNull(info.name),
                    phone = Optional.presentIfNotNull(info.phone)
                )
                val inputKitchen = InputStKitchen(
                    address = Optional.presentIfNotNull(iopAddress),
                    info = Optional.presentIfNotNull(inputInfo)
                )
                Helios(this@BuildKitchenScenario).beSaveWorkstationKitchen(
                    wkStation!!.uniqueId ?: "", inputKitchen
                ) { status, _ ->
                    if (status == ApiStatus.SUCCESS) {
                        CoroutineScope(Dispatchers.Main).launch {
                            complete?.let { it() }
                        }
                    }
                }
            }
        }

    }
    override fun beCollectParcels(complete: (ObStKitchen) -> Unit) {
        tell { actCollectParcels(complete) }
    }

    override fun beCheckInput(phone: String, complete: (Boolean) -> Unit) {
        tell { actCheckInput(phone, complete) }
    }

    override fun beSaveKitchen(info: KitchenInfo, floor: String, complete: (() -> Unit)?) {
        tell { actSaveKitchen(info, floor, complete) }
    }
}