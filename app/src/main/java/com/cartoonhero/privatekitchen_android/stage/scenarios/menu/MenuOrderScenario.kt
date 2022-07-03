package com.cartoonhero.privatekitchen_android.stage.scenarios.menu

import com.cartoonhero.privatekitchen_android.actors.Transformer
import com.cartoonhero.privatekitchen_android.actors.apolloApi.ApiStatus
import com.cartoonhero.privatekitchen_android.actors.apolloApi.Icarus
import com.cartoonhero.privatekitchen_android.actors.archmage.Archmage
import com.cartoonhero.privatekitchen_android.actors.archmage.LiveScene
import com.cartoonhero.privatekitchen_android.actors.archmage.Teleporter
import com.cartoonhero.privatekitchen_android.props.entities.*
import com.cartoonhero.privatekitchen_android.stage.scene.menu.MenuOrderDirector
import com.cartoonhero.theatre.Courier
import com.cartoonhero.theatre.Scenario
import graphqlApollo.client.FindMenuQuery
import graphqlApollo.operation.type.InputOrderItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class MenuOrderScenario: Scenario(), MenuOrderDirector {
    private var odrData: OrderData? = null
    private var orderItems: List<InputOrderItem> = listOf()
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
                when(val content = parcel.content) {
                    is OrderData -> {
                        odrData = content
                        callFindMenuApi(content.kitchen.uniqueId ?: "")
                    }
                    is List<*> -> orderItems = content.filterIsInstance<InputOrderItem>()
                    is OrderStorage -> odrStore = content
                }
            }
        }
    }
    private fun actParseToMenuItemVM(items: List<MenuItem>) {

    }
    private fun actSetOrder(order: OrderItem, complete: (Boolean) -> Unit) {

    }
    private fun actPackOrders(complete: (() -> Unit)?) {

    }
    private fun actPackFormData(complete: (() -> Unit)?) {

    }
    private fun actLowerCurtain() {
        archmage.beShutOff()
    }

    /** ------------------------------------------------------------------------------------------------ **/

    private fun callFindMenuApi(kitchenId: String) {
        Icarus(this).beFindMenu(kitchenId) { status, respObj ->
            when(status) {
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

    override fun beSetOrder(order: OrderItem, complete: (Boolean) -> Unit) {
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