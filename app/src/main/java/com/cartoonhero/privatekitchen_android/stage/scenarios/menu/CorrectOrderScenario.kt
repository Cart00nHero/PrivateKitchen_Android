package com.cartoonhero.privatekitchen_android.stage.scenarios.menu

import com.cartoonhero.privatekitchen_android.actors.archmage.*
import com.cartoonhero.privatekitchen_android.props.CorrectCompletion
import com.cartoonhero.privatekitchen_android.props.entities.*
import com.cartoonhero.privatekitchen_android.stage.scene.menu.CorrectOrderDirector
import com.cartoonhero.theatre.Courier
import com.cartoonhero.theatre.Parcel
import com.cartoonhero.theatre.Scenario
import graphqlApollo.operation.type.InputChoice
import graphqlApollo.operation.type.InputOrderItem
import kotlinx.coroutines.*

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class CorrectOrderScenario : Scenario(), CorrectOrderDirector {
    private lateinit var orderData: OrderData
    private lateinit var odrStore: OrderStorage
    private var orders: MutableList<InputOrderItem> = mutableListOf()
    private var receipt: Parcel<OrderData>? = null
    private val archmage: Archmage by lazy {
        Archmage(this)
    }

    private fun actShowTime(teleport: Teleporter) {
        archmage.beSetWaypoint(teleport)
        archmage.beSetTeleportation(teleportation)
    }

    private fun actCollectErrorOrders() {
        val collectJob: Deferred<List<Int>> = async {
            var errIndexes: List<Int> = listOf()
            val pSet = Courier(this@CorrectOrderScenario).beClaim()
            for (parcel in pSet) {
                when (val content = parcel.content) {
                    is OrderData -> {
                        orderData = OrderData(kitchen = content.kitchen)
                        orders = content.orders
                        odrStore = content.storage
                    }
                    is List<*> -> {
                        errIndexes = content.filterIsInstance<Int>()
                    }
                }
            }
            errIndexes
        }
        val parseJob: Deferred<List<ErrorOrder>> = async {
            val errOrders = mutableListOf<ErrorOrder>()
            val indexes: List<Int> = collectJob.await()
            for (idx in indexes) {
                val order = orders[idx]
                val amount: Int = odrStore.sumOfChosen[order.item.spotId] ?: 0
                errOrders.add(
                    ErrorOrder(index = idx, order = order, customAmount = amount)
                )
            }
            errOrders
        }
        launch {
            val result = parseJob.await()
            archmage.beChant(LiveScene(prop = result))
        }
    }

    private fun actPackErrorOrders(error: ErrorOrder, complete: (() -> Unit)?) {
        launch {
            val recipient = "CustomOdrScenario"
            val courier = Courier(this@CorrectOrderScenario)
            courier.beApply(error, recipient)
            courier.beApply(error.index, recipient)
            courier.beApply(odrStore, recipient)
            withContext(Dispatchers.Main) {
                complete?.let { it() }
            }
        }
    }

    private fun actCheckErrors(errors: List<ErrorOrder>, complete: (List<ErrorOrder>) -> Unit) {
        beSendBack(null)
        val remain: MutableList<ErrorOrder> = mutableListOf()
        for (errorOdr in errors) {
            val order = orders[errorOdr.index]
            val amount: Int = odrStore.sumOfChosen[order.item.spotId] ?: 0
            if (order.quantity < amount) {
                remain.add(errorOdr)
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            complete(remain)
        }
    }

    private fun actSendBack(complete: (() -> Unit)?) {
        launch {
            val courier = Courier(this@CorrectOrderScenario)
            val recipient = "OrderAmountScenario"
            if (receipt != null) {
                courier.beCancel(recipient, receipt!!)
            }
            orderData.orders.clear()
            orderData.orders.addAll(orders)
            orderData.storage = odrStore
            courier.beApply(orderData, recipient)
            withContext(Dispatchers.Main) {
                complete?.let { it() }
            }
        }
    }

    private fun actLowerCurtain() {
        archmage.beShutOff()
    }

    /** --------------------------------------------------------------------------------------------------- **/

    private val teleportation: Teleporter = object : Teleporter {
        override fun beSpellCraft(spell: Spell) {
            if (spell is MassTeleport) {
                when (val cargo = spell.cargo) {
                    is CalculateCustom -> {
                        val newOrder: InputOrderItem = cargo.odrItem
                        orders[cargo.index] = newOrder
                        val choices: List<InputChoice?> = newOrder.customize.getOrNull() ?: listOf()
                        if (choices.isNotEmpty()) {
                            var costValue = 0.0
                            for (chosen in choices) {
                                costValue += chosen?.cost ?: 0.0
                            }
                            odrStore.costs[newOrder.item.spotId]?.customCosts = costValue
                        }
                    }
                    is UpdateTotalChosen -> {
                        if (cargo.total <= 0) {
                            odrStore.sumOfChosen.remove(cargo.spotId)
                        } else {
                            odrStore.sumOfChosen[cargo.spotId] = cargo.total
                        }
                        archmage.beChant(LiveScene(CorrectCompletion()))
                    }
                }
            }
        }
    }

    /** --------------------------------------------------------------------------------------------------- **/

    override fun beShowTime(teleport: Teleporter) {
        tell { actShowTime(teleport) }
    }

    override fun beCollectErrorOrders() {
        tell { actCollectErrorOrders() }
    }

    override fun bePackErrorOrders(error: ErrorOrder, complete: (() -> Unit)?) {
        tell { actPackErrorOrders(error, complete) }
    }

    override fun beCheckErrors(errors: List<ErrorOrder>, complete: (List<ErrorOrder>) -> Unit) {
        tell { actCheckErrors(errors, complete) }
    }

    override fun beSendBack(complete: (() -> Unit)?) {
        tell { actSendBack(complete) }
    }

    override fun beLowerCurtain() {
        tell { actLowerCurtain() }
    }
}