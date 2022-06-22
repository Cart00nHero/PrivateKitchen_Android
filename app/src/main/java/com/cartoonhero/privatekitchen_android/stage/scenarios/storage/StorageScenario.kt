package com.cartoonhero.privatekitchen_android.stage.scenarios.storage

import com.apollographql.apollo3.api.Optional
import com.cartoonhero.privatekitchen_android.actors.Transformer
import com.cartoonhero.privatekitchen_android.actors.apolloApi.ApiStatus
import com.cartoonhero.privatekitchen_android.actors.apolloApi.Helios
import com.cartoonhero.privatekitchen_android.actors.archmage.Archmage
import com.cartoonhero.privatekitchen_android.actors.archmage.LiveScene
import com.cartoonhero.privatekitchen_android.actors.archmage.Teleporter
import com.cartoonhero.privatekitchen_android.actors.objBox.ObDb
import com.cartoonhero.privatekitchen_android.patterns.Transcribe
import com.cartoonhero.privatekitchen_android.patterns.obTransfer.StorageTransfer
import com.cartoonhero.privatekitchen_android.props.entities.Storehouse
import com.cartoonhero.privatekitchen_android.props.obEntities.*
import com.cartoonhero.privatekitchen_android.stage.scene.storage.StorageDirector
import com.cartoonhero.theatre.Courier
import com.cartoonhero.theatre.Scenario
import graphqlApollo.operation.FindStorehouseQuery
import graphqlApollo.operation.type.InputMenuItem
import graphqlApollo.operation.type.InputOption
import graphqlApollo.operation.type.InputStorehouse
import kotlinx.coroutines.*

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class StorageScenario: Scenario(), StorageDirector {
    private var stationId: String = ""
    private var obStore: ObStorehouse? = null
    private var customItem: ObMenuItem? = null

    private val archmage: Archmage by lazy {
        Archmage(this)
    }
    private fun actShowTime(teleporter: Teleporter) {
        archmage.beSetWaypoint(teleporter)
    }
    private fun actCollectParcels() {
        launch {
            val pSet = Courier(this@StorageScenario).beClaim()
            for (parcel in pSet) {
                when(val content = parcel.content) {
                    is ObWorkstation -> {
                        stationId = content.uniqueId ?: ""
                        beRestoreData()
                    }
                }
            }
        }
    }
    private fun actRestoreData() {
        val saveStoreJob: Deferred<ObStorehouse?> = async {
            val storeBox = ObDb().beTakeBox(ObStorehouse::class.java)
            val query = storeBox.query(
                ObStorehouse_.ownerId.equal(stationId)
            ).build()
            return@async query.findUnique()
        }
        launch {
            val obHouse = saveStoreJob.await()
            when {
                obHouse != null -> {
                    obStore = obHouse
                    archmage.beChant(LiveScene(prop = obHouse))
                }
                else -> {
                    Helios(this@StorageScenario).beFindStorehouse(
                        stationId
                    ) { status,foundData ->
                        when(status) {
                            ApiStatus.SUCCESS -> {
                                if (foundData != null) {
                                    launch {
                                        val newHouse = Transformer().beTransfer<
                                                FindStorehouseQuery.FindStorehouse,
                                                Storehouse>(foundData)
                                        if (newHouse != null) {
                                            val transfer = StorageTransfer(this@StorageScenario)
                                            transfer.beSet(newHouse)
                                            obStore = transfer.beTransfer()
                                            if (obStore != null) archmage.beChant(LiveScene(obStore!!))
                                        }
                                    }
                                }
                            }
                            ApiStatus.FAILED -> print("Error")
                        }
                    }
                }
            }
        }
    }
    private fun actUploadData(complete: (Boolean) -> Unit) {
        val mainScope = CoroutineScope(Dispatchers.Main)
        if (obStore == null) {
            mainScope.launch { complete(false) }
            return
        }
        launch {
            val inputOpts = prepareOptionInputs(obStore!!.options)
            val inputItems = prepareItemInputs()
            val storeInput = InputStorehouse(
                options = Optional.presentIfNotNull(inputOpts),
                items = Optional.presentIfNotNull(inputItems)
            )
            val sBox = ObDb().beTakeBox(ObStorehouse::class.java)
            sBox.put(obStore!!)
            Helios(this@StorageScenario).beUpdateStorehouse(
                stationId, storeInput
            ) { status, _ ->
                when(status) {
                    ApiStatus.SUCCESS -> {
                        mainScope.launch { complete(true) }
                    }
                    ApiStatus.FAILED -> {
                        mainScope.launch { complete(false) }
                        print("UpdateStorehouse Boom")
                    }
                }
            }
        }
    }
    private fun actCheckPickUp(source: Set<Long>, spotId: Long, complete: (Boolean) -> Unit) {
        val picked: Boolean = source.contains(spotId)
        CoroutineScope(Dispatchers.Main).launch { complete(picked) }
    }
 /** ---------------------------------------------------------------------------------------------------------- **/
    private suspend fun prepareOptionInputs(options: List<ObOption>): List<InputOption> {
        val optInputs: MutableList<InputOption> = mutableListOf()
        for (obOpt in options) {
            val titleInput = Transcribe(this).beLocalizedTextTo(obOpt.beTitle())
            val optInput = InputOption(
                price = obOpt.price ?: 0.0,
                spotId = obOpt.spotId.toString(),
                titleText = Optional.presentIfNotNull(titleInput)
            )
            optInputs.add(optInput)
        }
        return optInputs
    }
    private suspend fun prepareItemInputs(): List<InputMenuItem> {
        val obItems: List<ObMenuItem> = obStore!!.items
        val itemInputs: MutableList<InputMenuItem> = mutableListOf()
        for (obItem in obItems) {
            val nameInput = Transcribe(this).beLocalizedTextTo(obItem.beName())
            val introInput = Transcribe(this).beLocalizedTextTo(obItem.beIntro())
            val itInput = InputMenuItem(
                customizations = listOf(), photo = Optional.presentIfNotNull(obItem.photo),
                price = obItem.price, sequence = obItem.sequence,
                spotId = obItem.spotId.toString(), nameText = Optional.presentIfNotNull(nameInput),
                introText = Optional.presentIfNotNull(introInput)
            )
            itemInputs.add(itInput)
        }
        return itemInputs
    }
/** ---------------------------------------------------------------------------------------------------------- **/
    override fun beShowTime(teleporter: Teleporter) {
        tell { actShowTime(teleporter) }
    }

    override fun beCollectParcels() {
        tell { actCollectParcels() }
    }

    override fun beRestoreData() {
        tell { actRestoreData() }
    }

    override fun beUploadData(complete: (Boolean) -> Unit) {
        tell { actUploadData(complete) }
    }

    override fun beCheckPickUp(source: Set<Long>, spotId: Long, complete: (Boolean) -> Unit) {
        tell { actCheckPickUp(source, spotId, complete) }
    }

    override fun beSaveCustomItem(optIds: Set<Long>) {
        TODO("Not yet implemented")
    }

    override fun beSaveData(complete: (() -> Unit)?) {
        TODO("Not yet implemented")
    }

    override fun beLowerCurtain() {
        TODO("Not yet implemented")
    }
}