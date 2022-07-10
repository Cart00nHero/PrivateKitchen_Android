package com.cartoonhero.privatekitchen_android.stage.scene.menu

import com.cartoonhero.privatekitchen_android.actors.archmage.Teleporter
import com.cartoonhero.privatekitchen_android.props.entities.ErrorOrder
import com.cartoonhero.privatekitchen_android.props.entities.MenuItem
import com.cartoonhero.privatekitchen_android.props.entities.OrderItem
import com.cartoonhero.privatekitchen_android.props.obEntities.ObCategory
import com.cartoonhero.privatekitchen_android.props.obEntities.ObMenuItem
import com.cartoonhero.privatekitchen_android.props.obEntities.ObPage
import graphqlApollo.operation.type.InputChoice
import graphqlApollo.operation.type.InputOption
import graphqlApollo.operation.type.InputOrderItem
import java.lang.Error

interface EditMenuDirector {
    fun beShowTime(teleport: Teleporter)
    fun beCollectParcels()
    fun beAddPage(pagination: Int, complete:(() -> Unit)?)
    fun beAddCategory(page: ObPage, complete: (() -> Unit)?)
    fun beRemove(page: ObPage)
    fun beRemove(page: ObPage, atCIdx: Int)
    fun beSetEdit(category: ObCategory, page: ObPage)
    fun beSaveCategory(title: String, items: List<ObMenuItem>)
    fun beUpdate(title: String, category: ObCategory)
    fun bePickUnPick(item: ObMenuItem)
    fun beUpload(complete:(Boolean) -> Unit)
    fun bePublish(complete:(Boolean) -> Unit)
    fun beLowerCurtain()
}
interface MenuOrderDirector {
    fun beShowTime(teleport: Teleporter)
    fun beCollectParcels()
    fun beParseToMenuItemVM(items: List<MenuItem>)
    fun beSetOrder(order: OrderItem, complete: ((Boolean) -> Unit)?)
    fun bePackOrders(complete: (() -> Unit)?)
    fun bePackFormData(complete: (() -> Unit)?)
    fun beLowerCurtain()
}
interface OrderAmountDirector {
    fun beShowTime(teleport: Teleporter)
    fun beCollectParcels(complete: (List<InputOrderItem>, List<MenuItem>) -> Unit)
    fun beCalculateItemCost(item: MenuItem, sum: Int)
    fun beSendBackOrderInputs(orders: List<InputOrderItem>, complete: (() -> Unit)?)
    fun beCheckOrders(orders: List<InputOrderItem>, complete: (List<Int>) -> Unit)
    fun bePackCustomItem(item: InputOrderItem, index: Int,complete: (() -> Unit)?)
    fun beLowerCurtain()
}
interface CustomOdrDirector {
    fun beCollectParcels(complete: (InputOrderItem, Int) -> Unit)
    fun beAddChoice(choice: InputChoice, complete: (() -> Unit)?)
    fun beRemoveTab(tabIdx: Int, chosen: InputChoice, complete: ((idx: Int, amount: Int) -> Unit)?)
    fun bePickOption(
        option: InputOption, choice: InputChoice, tabIdx: Int,
        complete: (InputChoice) -> Unit
    )
    fun beUnPickOption(
        option: InputOption, idx: Int, choice: InputChoice,
        tabIdx: Int, complete: (InputChoice, Int) -> Unit
    )
    fun beOrderAmount(complete: (Int) -> Unit)
    fun beIncrease(
        value: Int, chosen: InputChoice, idx: Int,
        complete: (Int, InputChoice) -> Unit
    )
    fun beCalculateChosen(chosen: InputChoice, tabIdx: Int)
    fun beSyncTabIndex(tabIndex: Int, complete: (Int, InputChoice) -> Unit)
    fun beStoreTotalRemain(remain: Int)
    fun beCustomize(complete: (() -> Unit)?)
}

interface CorrectOrderDirector {
    fun beShowTime(teleport: Teleporter)
    fun beCollectErrorOrders()
    fun bePackErrorOrders(error: ErrorOrder, complete: (() -> Unit)?)
    fun beCheckErrors(errors: List<ErrorOrder>, complete: (List<ErrorOrder>) -> Unit)
    fun beSendBack(complete: (() -> Unit)?)
    fun beLowerCurtain()
}