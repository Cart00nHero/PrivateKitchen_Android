package com.cartoonhero.privatekitchen_android.patterns.obTransfer

import com.cartoonhero.privatekitchen_android.actors.Transformer
import com.cartoonhero.privatekitchen_android.actors.objBox.ObDb
import com.cartoonhero.privatekitchen_android.props.entities.*
import com.cartoonhero.privatekitchen_android.props.obEntities.*
import com.cartoonhero.theatre.Pattern
import com.cartoonhero.theatre.Scenario
import kotlinx.coroutines.*

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class WkStationTransfer(attach: Scenario) : Pattern(attach) {
    private lateinit var fromObj: Workstation
    private lateinit var toObj: ObWorkstation

    private fun actSet(from: Workstation) {
        this.fromObj = from
    }
    private fun actTransfer(
        actJob: CompletableDeferred<ObWorkstation?>
    ) {
        launch {
            toObj = ObWorkstation(
                id = 0,uniqueId = fromObj.uniqueId,
                chefId = fromObj.chefId,
                kitchen_id = fromObj.kitchenId
            )
            if (syncWorkStation()) {
                startTransfer()
                val wkStBox = ObDb().beTakeBox(ObWorkstation::class.java)
                val query = wkStBox.query(
                    ObWorkstation_.uniqueId.equal(fromObj.uniqueId ?: "")
                ).build()
                val found = query.findUnique()
                actJob.complete(found)
            }
        }
    }
    private suspend fun startTransfer() {
        if (fromObj.info != null) {
            val infoJson = Transformer().beToJson(fromObj.info)
            toObj.info = infoJson
        }
        if (fromObj.kitchen != null) {
            toObj.kitchen.target = transKitchen()
        }
        val wkStBox = ObDb().beTakeBox(ObWorkstation::class.java)
        wkStBox.put(toObj)
    }
    private suspend fun cleanDB() {
        val stBox = ObDb().beTakeBox(ObWorkstation::class.java)
        val kBox = ObDb().beTakeBox(ObStKitchen::class.java)
        kBox.removeAll()
        stBox.removeAll()
    }
    private suspend fun syncWorkStation(): Boolean {
        val wkStBox = ObDb().beTakeBox(ObWorkstation::class.java)
        val query = wkStBox.query(
            ObWorkstation_.uniqueId.equal(fromObj.uniqueId ?: "")
        ).build()
        val found = query.findUnique()
        if (found != null) toObj.id = found.id
        return true
    }
    private suspend fun transKitchen(): ObStKitchen {
        val kitchen: StKitchen = fromObj.kitchen!!
        val obStKitchen: ObStKitchen = toObj.kitchen.target ?: ObStKitchen(id = 0)
        if (kitchen.info != null) {
            val tempInfo = Transformer().beToJson(kitchen.info)
            obStKitchen.info = tempInfo
        }
        if (kitchen.address != null) {
            val addressMap: Map<String,Any> = Transformer().beEntityToMap(
                kitchen.address!!
            ) ?: mapOf()
            val modifyMap: MutableMap<String,Any> = addressMap.toMutableMap()
            modifyMap["id"] = 0
            val obAddress: ObAddress = Transformer().beMapToEntity<Any,ObAddress>(
                modifyMap
            ) ?: ObAddress(id = 0)
            obAddress.id = obStKitchen.address.target.id
            obStKitchen.address.target = obAddress
        }
        return obStKitchen
    }

    fun beSet(from: Workstation) {
        tell {
            actSet(from)
        }
    }
    suspend fun beTransfer(): ObWorkstation? {
        val actorJob = CompletableDeferred<ObWorkstation?>()
        tell {
            actTransfer(actorJob)
        }
        return actorJob.await()
    }
}
