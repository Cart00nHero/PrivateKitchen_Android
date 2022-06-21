package com.cartoonhero.privatekitchen_android.patterns.obTransfer

import com.cartoonhero.privatekitchen_android.actors.Transformer
import com.cartoonhero.privatekitchen_android.actors.generator.Generator
import com.cartoonhero.privatekitchen_android.actors.objBox.ObDb
import com.cartoonhero.privatekitchen_android.props.entities.GQOption
import com.cartoonhero.privatekitchen_android.props.entities.MenuItem
import com.cartoonhero.privatekitchen_android.props.entities.Storehouse
import com.cartoonhero.privatekitchen_android.props.obEntities.*
import com.cartoonhero.theatre.Pattern
import com.cartoonhero.theatre.Scenario
import io.objectbox.query.Query
import kotlinx.coroutines.*

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class StorageTransfer(attach: Scenario) : Pattern(attach) {
    private lateinit var fromObj: Storehouse
    private lateinit var toObj: ObStorehouse

    private fun actSet(from: Storehouse) {
        fromObj = from
        toObj = ObStorehouse(id = 0, ownerId = from.ownerId)
    }
    private fun actTransfer(myJob: CompletableDeferred<ObStorehouse?>) {
        launch {
            if (cleanDB()) {
                val storeBox = ObDb().beTakeBox(ObStorehouse::class.java)
                storeBox.put(toObj)
                val found = storeBox.all.first()
                if (found != null) {
                    toObj = found
                    startTransfer()
                    val query = storeBox.query(
                        ObStorehouse_.ownerId.equal(fromObj.ownerId ?: "")
                    ).build()
                    myJob.complete(query.findUnique())
                }
            }
        }
    }
    private suspend fun cleanDB(): Boolean {
        val storeBox = ObDb().beTakeBox(ObStorehouse::class.java)
        val itemBox = ObDb().beTakeBox(ObDiningWay::class.java)
        val optBox = ObDb().beTakeBox(ObOption::class.java)
        val dBox = ObDb().beTakeBox(ObDiningWay::class.java)
        dBox.removeAll()
        optBox.removeAll()
        itemBox.removeAll()
        storeBox.removeAll()
        return true
    }
    private suspend fun startTransfer() {
        var obOptions: List<ObOption> = listOf()
        var items: List<ObMenuItem> = listOf()
        val optJob: Deferred<List<ObOption>> = async {
            return@async transfer(fromObj.options ?: listOf())
        }
        obOptions = optJob.await()
        items = transfer(fromObj.items ?: listOf())
        val storeBox = ObDb().beTakeBox(ObStorehouse::class.java)
        toObj.options.addAll(obOptions)
        toObj.items.addAll(items)
        storeBox.put(toObj)
    }
    @JvmName("transfer1")
    private suspend fun transfer(options: List<GQOption>): List<ObOption> {
        val obOptions: MutableList<ObOption> = mutableListOf()
        for (i in options.indices) {
            val option: GQOption = options[i]
            val tempId: Long = Generator().beSpotId()
            val spotId: Long = option.spotId?.toLong() ?: tempId
            val obOpt = ObOption(
                id = 0, spotId = spotId,
                sequence = i, price = option.price
            )
            if (option.titleText != null) {
                val titleJson: String = Transformer().beToJson(option.titleText)
                obOpt.titleText = titleJson
            }
            obOptions.add(obOpt)
        }
        val optBox = ObDb().beTakeBox(ObOption::class.java)
        optBox.put(obOptions)
        return optBox.all
    }

    private suspend fun transfer(items: List<MenuItem>): List<ObMenuItem> {
        val obItems: MutableList<ObMenuItem> = mutableListOf()
        val optRecords: MutableMap<Long, List<ObOption>> = mutableMapOf()
        for (i in items.indices) {
            val item: MenuItem = items[i]
            val tempId: Long = Generator().beSpotId()
            val spotId: Long = item.spotId?.toLong() ?: tempId
            val obItem = ObMenuItem(
                id = 0, spotId = spotId, sequence = i,
                price = item.price ?: 0.0, photo = item.photo ?: "",
                quota = item.quota ?: 0
            )
            val customJob: Deferred<List<ObOption>> = async {
                val gqOptions: List<GQOption> = item.customizations ?: listOf()
                val customOpts: MutableList<ObOption> = mutableListOf()
                for (gqOpt in gqOptions) {
                    val obOpt = findCustomOpt(gqOpt)
                    if (obOpt != null) {
                        customOpts.add(obOpt)
                    }
                }
                return@async customOpts
            }
            if (item.nameText != null) {
                val nameJson: String = Transformer().beToJson(item.nameText)
                obItem.nameText = nameJson
            }
            if (item.introText != null) {
                val introJson: String = Transformer().beToJson(item.introText)
                obItem.introText = introJson
            }
            optRecords[obItem.spotId] = customJob.await()
            obItems.add(obItem)
        }
        val customItems: MutableList<ObMenuItem> = mutableListOf()
        val itemBox = ObDb().beTakeBox(ObMenuItem::class.java)
        itemBox.put(obItems)
        for (itemId in optRecords.keys) {
            val itemQuery = itemBox.query(
                ObMenuItem_.spotId.equal(itemId)
            ).build()
            val customIt = itemQuery.findUnique()
            if (customIt != null) {
                optRecords[itemId]?.let { customIt.options.addAll(it) }
                customItems.add(customIt)
            }
        }
        itemBox.put(customItems)
        return itemBox.all
    }
    private suspend fun findCustomOpt(option: GQOption): ObOption? {
        val tempId: Long = Generator().beSpotId()
        val spotId: Long = option.spotId?.toLong() ?: tempId
        val optBox = ObDb().beTakeBox(ObOption::class.java)
        val query: Query<ObOption> = optBox.query(
            ObOption_.spotId.equal(spotId)
        ).build()
        return query.findUnique()
    }

    fun beSet(from: Storehouse) {
        tell {
            actSet(from)
        }
    }
    suspend fun beTransfer(): ObStorehouse? {
        val actorJob = CompletableDeferred<ObStorehouse?>()
        tell { actTransfer(actorJob) }
        return actorJob.await()
    }
}