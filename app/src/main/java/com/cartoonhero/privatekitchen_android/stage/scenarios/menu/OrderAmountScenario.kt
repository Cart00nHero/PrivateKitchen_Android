package com.cartoonhero.privatekitchen_android.stage.scenarios.menu

import com.cartoonhero.privatekitchen_android.actors.archmage.*
import com.cartoonhero.privatekitchen_android.props.entities.*
import com.cartoonhero.privatekitchen_android.stage.scene.menu.OrderAmountDirector
import com.cartoonhero.theatre.Courier
import com.cartoonhero.theatre.Scenario
import graphqlApollo.operation.type.InputChoice
import graphqlApollo.operation.type.InputOrderItem
import kotlinx.coroutines.*

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class OrderAmountScenario : Scenario(), OrderAmountDirector, Teleporter {
    lateinit var orderData: OrderData
    lateinit var odrStore: OrderStorage
    private val archmage: Archmage by lazy {
        Archmage(this)
    }

    private fun actCollectParcels(complete: (List<InputOrderItem>, List<MenuItem>) -> Unit) {
        launch {
            val pSet = Courier(this@OrderAmountScenario).beClaim()
            for (parcel in pSet) {
                when (val content = parcel.content) {
                    is OrderData -> {
                        orderData = OrderData(kitchen = content.kitchen)
                        odrStore = content.storage
                        archmage.beChant(
                            LiveScene(DisplayOrderData(orderData.orders, odrStore.menuItems))
                        )
                    }
                }
            }
        }
    }

    private fun actCalculateItemCost(item: MenuItem, sum: Int) {
        if (item.spotId != null) {
            val spotText: String = item.spotId
            val value: Double = (item.price ?: 0.0) * sum.toDouble()
            odrStore.costs[spotText]?.itemCosts = value
        }
    }

    private fun actSendBackOrderInputs(orders: List<InputOrderItem>, complete: (() -> Unit)?) {
        launch {
            val courier = Courier(this@OrderAmountScenario)
            courier.beApply(orders, "MenuOrderScenario")
            courier.beApply(odrStore, "MenuOrderScenario")
            withContext(Dispatchers.Main) {
                complete?.let { it() }
            }
        }
    }

    private fun actCheckOrders(orders: List<InputOrderItem>, complete: (List<Int>) -> Unit) {
        val checkJob: Deferred<List<Int>> = async {
            val errorIndexes = mutableListOf<Int>()
            for (i in orders.indices) {
                val odr = orders[i]
                val customAmount: Int = odrStore.sumOfChosen[odr.item.spotId] ?: 0
                if (odr.quantity < customAmount) {
                    errorIndexes.add(i)
                }
            }
            errorIndexes
        }
        launch {
            orderData.orders.addAll(orders)
            orderData.storage = odrStore
            val courier = Courier(this@OrderAmountScenario)
            val recipient = "CorrectOrderScenario"
            val errIndexes = checkJob.await()
            if (errIndexes.isNotEmpty()) {
                courier.beApply(errIndexes, recipient)
                courier.beApply(orderData, recipient)
            }
            withContext(Dispatchers.Main) {
                complete(errIndexes)
            }
        }
    }

    private fun actPackCustomItem(item: InputOrderItem, index: Int, complete: (() -> Unit)?) {
        launch {
            val recipient = "CustomOdrScenario"
            val courier = Courier(this@OrderAmountScenario)
            courier.beApply(item, recipient)
            courier.beApply(index, recipient)
            courier.beApply(odrStore, recipient)
            withContext(Dispatchers.Main) {
                complete?.let { it() }
            }
        }
    }

    private fun actLowerCurtain() {
        archmage.beShutOff()
    }


    /** ------------------------------------------------------------------------------------------------ **/

    override fun beShowTime(teleport: Teleporter) {
        archmage.beSetWaypoint(teleport)
        archmage.beSetTeleportation(this)
    }

    override fun beCollectParcels(complete: (List<InputOrderItem>, List<MenuItem>) -> Unit) {
        tell { actCollectParcels(complete) }
    }

    override fun beCalculateItemCost(item: MenuItem, sum: Int) {
        tell { actCalculateItemCost(item, sum) }
    }

    override fun beSendBackOrderInputs(orders: List<InputOrderItem>, complete: (() -> Unit)?) {
        tell { actSendBackOrderInputs(orders, complete) }
    }

    override fun beCheckOrders(orders: List<InputOrderItem>, complete: (List<Int>) -> Unit) {
        tell { actCheckOrders(orders, complete) }
    }

    override fun bePackCustomItem(item: InputOrderItem, index: Int, complete: (() -> Unit)?) {
        tell { actPackCustomItem(item, index, complete) }
    }

    override fun beLowerCurtain() {
        tell { actLowerCurtain() }
    }

    override fun beSpellCraft(spell: Spell) {
        if (spell is MassTeleport) {
            when(val stuff = spell.cargo) {
                is CalculateCustom -> {
                    val order: InputOrderItem = stuff.odrItem
                    val choices: List<InputChoice?> = order.customize.getOrNull() ?: listOf()
                    var costValue: Double = 0.0
                    for (chosen in choices) {
                        costValue += chosen?.cost ?: 0.0
                    }
                    odrStore.costs[order.item.spotId]?.customCosts  = costValue
                    archmage.beChant(LiveList(stuff.index, stuff.index))
                }
                is UpdateTotalChosen -> {
                    if (stuff.total <= 0) {
                        odrStore.sumOfChosen.remove(stuff.spotId)
                    } else {
                        odrStore.sumOfChosen[stuff.spotId] = stuff.total
                    }
                }
            }
        }
    }

}