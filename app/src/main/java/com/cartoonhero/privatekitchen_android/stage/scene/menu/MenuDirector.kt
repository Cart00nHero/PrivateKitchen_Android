package com.cartoonhero.privatekitchen_android.stage.scene.menu

import com.cartoonhero.privatekitchen_android.actors.archmage.Teleporter
import com.cartoonhero.privatekitchen_android.props.entities.MenuItem
import com.cartoonhero.privatekitchen_android.props.entities.OrderItem
import com.cartoonhero.privatekitchen_android.props.obEntities.ObCategory
import com.cartoonhero.privatekitchen_android.props.obEntities.ObMenuItem
import com.cartoonhero.privatekitchen_android.props.obEntities.ObPage

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