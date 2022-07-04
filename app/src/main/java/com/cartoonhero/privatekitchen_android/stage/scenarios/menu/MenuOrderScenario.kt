package com.cartoonhero.privatekitchen_android.stage.scenarios.menu

import com.apollographql.apollo3.api.Optional
import com.cartoonhero.privatekitchen_android.actors.Transformer
import com.cartoonhero.privatekitchen_android.actors.apolloApi.ApiStatus
import com.cartoonhero.privatekitchen_android.actors.apolloApi.Icarus
import com.cartoonhero.privatekitchen_android.actors.archmage.Archmage
import com.cartoonhero.privatekitchen_android.actors.archmage.LiveScene
import com.cartoonhero.privatekitchen_android.actors.archmage.Teleporter
import com.cartoonhero.privatekitchen_android.patterns.Transcribe
import com.cartoonhero.privatekitchen_android.props.entities.*
import com.cartoonhero.privatekitchen_android.stage.scene.menu.MenuOrderDirector
import com.cartoonhero.theatre.Courier
import com.cartoonhero.theatre.Scenario
import graphqlApollo.client.FindMenuQuery
import graphqlApollo.operation.type.InputMenuItem
import graphqlApollo.operation.type.InputOption
import graphqlApollo.operation.type.InputOrderItem
import kotlinx.coroutines.*

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class MenuOrderScenario : Scenario(), MenuOrderDirector {
    private var odrData: OrderData? = null
    private var orderItems: MutableList<InputOrderItem> = mutableListOf()
    private var odrStore: OrderStorage = OrderStorage()
    private val archmage: Archmage by lazy {
        Archmage(this)
    }

    private fun actShowTime(teleport: Teleporter) {
        archmage.beSetWaypoint(teleport)
    }

    private fun actCollectParcels() {
        launch {
            val pSet = Courier(this@MenuOrderScenario).beClaim()
            for (parcel in pSet) {
                when (val content = parcel.content) {
                    is OrderData -> {
                        odrData = content
                        callFindMenuApi(content.kitchen.uniqueId ?: "")
                    }
                    is List<*> -> {
                        orderItems.clear()
                        orderItems.addAll(content.filterIsInstance<InputOrderItem>())
                    }
                    is OrderStorage -> odrStore = content
                }
            }
        }
    }

    private fun actParseToMenuItemVM(items: List<MenuItem>) {
        val itemVMs = mutableListOf<MenuItemVM>()
        for (item in items) {
            val selected = odrStore.menuItems.find {
                it.spotId?.toLongOrNull() == item.spotId?.toLongOrNull()
            } != null
            itemVMs.add(
                MenuItemVM(selected = selected, items = item)
            )
        }
        archmage.beChant(LiveScene(prop = itemVMs))
    }

    private fun actSetOrder(order: OrderItem, complete: ((Boolean) -> Unit)?) {
        val isContain: Boolean = orderItems.find { it.item.spotId == order.item.spotId } != null
        when (isContain) {
            true -> {
                val spotText = order.item.spotId
                if (spotText != null) {
                    odrStore.costs.remove(spotText)
                    odrStore.sumOfChosen.remove(spotText)
                }
                orderItems.removeIf {
                    it.item.spotId.toLongOrNull() == order.item.spotId?.toLongOrNull()
                }
            }
            else -> {
                val spotText = order.item.spotId
                if (spotText != null) {
                    val cost: OrderCost = odrStore.costs[spotText] ?: OrderCost(
                        itemCosts = order.item.price ?: 0.0
                    )
                    odrStore.costs[spotText] = cost
                    launch {
                        val inputOdr = parseOrderItem(order)
                        tell {
                            odrStore.menuItems.add(order.item)
                            orderItems.add(inputOdr)
                        }
                    }
                }
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            complete?.let { it(!isContain) }
        }
    }

    private fun actPackOrders(complete: (() -> Unit)?) {
        if (odrData == null) return
        if (orderItems.isEmpty()) return
        launch {
            odrData!!.orders = orderItems
            odrData!!.storage = odrStore
            Courier(this@MenuOrderScenario).beApply(
                odrData, "OrderAmountScenario"
            )
            withContext(Dispatchers.Main) {
                complete?.let { it() }
            }
        }
    }

    private fun actPackFormData(complete: (() -> Unit)?) {
        if (odrData == null) return
        launch {
            odrData!!.orders = orderItems
            odrData!!.storage = odrStore
            Courier(this@MenuOrderScenario).beApply(
                odrData, "FillFormScenario"
            )
            withContext(Dispatchers.Main) {
                complete?.let { it() }
            }
        }
    }

    private fun actLowerCurtain() {
        archmage.beShutOff()
    }

    /** ------------------------------------------------------------------------------------------------ **/

    suspend fun parseOrderItem(item: OrderItem): InputOrderItem {
        val menuJob: Deferred<InputMenuItem> = async {
            val menuIt: MenuItem = item.item
            val inputName = Transcribe(this@MenuOrderScenario).beLocalizedTextTo(
                menuIt.nameText ?: LocalizedText()
            )
            val inputIntro = Transcribe(this@MenuOrderScenario).beLocalizedTextTo(
                menuIt.introText ?: LocalizedText()
            )
            InputMenuItem(
                customizations = parseOptions(menuIt.customizations ?: listOf()),
                introText = Optional.presentIfNotNull(inputIntro),
                nameText = Optional.presentIfNotNull(inputName),
                photo = Optional.presentIfNotNull(menuIt.photo),
                price = menuIt.price ?: 0.0, sequence = menuIt.sequence ?: 0,
                spotId = menuIt.spotId ?: ""
            )
        }
        return InputOrderItem(
            categoryId = item.categoryId ?: "",
            item = menuJob.await(), pagination = item.pagination ?: 0,
            quantity = item.quantity ?: 0
        )
    }

    suspend fun parseOptions(options: List<GQOption>): List<InputOption> {
        val inputOpts = mutableListOf<InputOption>()
        for (option in options) {
            val optTitle = Transcribe(this).beLocalizedTextTo(
                option.titleText ?: LocalizedText()
            )
            val newInput = InputOption(
                price = option.price ?: 0.0,
                spotId = option.spotId ?: "",
                titleText = Optional.presentIfNotNull(optTitle)
            )
            inputOpts.add(newInput)
        }
        return inputOpts
    }

    private fun callFindMenuApi(kitchenId: String) {
        Icarus(this).beFindMenu(kitchenId) { status, respObj ->
            when (status) {
                ApiStatus.SUCCESS -> launch {
                    if (respObj != null) {
                        val aMenu = Transformer()
                            .beTransfer<FindMenuQuery.FindMenu, GQMenu>(respObj)
                        if (aMenu != null) {
                            archmage.beChant(LiveScene(prop = aMenu))
                        }
                    }
                }
                ApiStatus.FAILED -> print("beFindMenu explode")
            }
        }
    }

    /** ------------------------------------------------------------------------------------------------ **/

    override fun beShowTime(teleport: Teleporter) {
        tell { actShowTime(teleport) }
    }

    override fun beCollectParcels() {
        tell { actCollectParcels() }
    }

    override fun beParseToMenuItemVM(items: List<MenuItem>) {
        tell { actParseToMenuItemVM(items) }
    }

    override fun beSetOrder(order: OrderItem, complete: ((Boolean) -> Unit)?) {
        tell { actSetOrder(order, complete) }
    }

    override fun bePackOrders(complete: (() -> Unit)?) {
        tell { actPackOrders(complete) }
    }

    override fun bePackFormData(complete: (() -> Unit)?) {
        tell { actPackFormData(complete) }
    }

    override fun beLowerCurtain() {
        tell { actLowerCurtain() }
    }

}