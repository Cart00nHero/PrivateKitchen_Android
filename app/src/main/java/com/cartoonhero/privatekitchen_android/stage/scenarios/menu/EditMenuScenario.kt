package com.cartoonhero.privatekitchen_android.stage.scenarios.menu

import com.cartoonhero.privatekitchen_android.actors.archmage.Archmage
import com.cartoonhero.privatekitchen_android.actors.archmage.LiveScene
import com.cartoonhero.privatekitchen_android.actors.archmage.Teleporter
import com.cartoonhero.privatekitchen_android.actors.objBox.ObDb
import com.cartoonhero.privatekitchen_android.props.entities.UnPickedItems
import com.cartoonhero.privatekitchen_android.props.obEntities.*
import com.cartoonhero.privatekitchen_android.stage.scene.menu.EditMenuDirector
import com.cartoonhero.theatre.Courier
import com.cartoonhero.theatre.Scenario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class EditMenuScenario: Scenario(), EditMenuDirector {
    private var stationId: String = ""
    private var kitchen: ObStKitchen? = null
    private var tempMenu: ObTemplate? = null
    private var pageEditing: Boolean = false
    private val archmage: Archmage by lazy {
        Archmage(this)
    }

    private fun actShowTime(teleport: Teleporter) {
        archmage.beSetWaypoint(teleport)
    }
    private fun actCollectParcels() {
        launch {
            val pSet = Courier(this@EditMenuScenario).beClaim()
            for (parcel in pSet) {
                when(val content = parcel.content) {
                    is ObWorkstation -> {
                        stationId = content.uniqueId ?: ""
                        if (content.menu.target != null) {
                            tempMenu = content.menu.target
                        } else {
                            searchMenu()
                        }
                    }
                }
            }
        }
    }

    private suspend fun searchMenu() {
        val tempBox = ObDb().beTakeBox(ObTemplate::class.java)
        val query = tempBox.query(
            ObTemplate_.ownerId.equal(stationId)
        ).build()
        val found = query.findUnique()
        if (found == null) {
            val newMenu = ObTemplate(id = 0, ownerId = stationId)
            tempBox.put(newMenu)
            queryObTemplate()
        } else {
            tempMenu = found
        }
    }
    private suspend fun queryUnpicked(): List<ObMenuItem>? {
        val itemBox = ObDb().beTakeBox(ObMenuItem::class.java)
        val query = itemBox.query(
            ObMenuItem_.toCategoryId.equal(0)
        ).build()
        val foundList = query.find()
        archmage.beChant(LiveScene(UnPickedItems(foundList)))
        return foundList
    }
    private suspend fun cleanItem(categories: List<ObCategory>) {
    }
    private suspend fun queryObTemplate() {
        val tempBox = ObDb().beTakeBox(ObTemplate::class.java)
        val query = tempBox.query(
            ObTemplate_.ownerId.equal(stationId)
        ).build()
        val newMenu = query.findUnique()
        if (newMenu != null) {
            tempMenu = newMenu
            archmage.beChant(LiveScene(prop = newMenu))
        }
    }

    /** ----------------------------------------------------------------------------------------------------- **/

    override fun beShowTime(teleport: Teleporter) {
        tell { actShowTime(teleport) }
    }

    override fun beCollectParcels() {
        tell { actCollectParcels() }
    }

    override fun beAddPage(pagination: Int, complete: (() -> Unit)?) {
        TODO("Not yet implemented")
    }

    override fun beAddCategory(page: ObPage, complete: (() -> Unit)?) {
        TODO("Not yet implemented")
    }

    override fun beRemove(page: ObPage) {
        TODO("Not yet implemented")
    }

    override fun beRemove(page: ObPage, atCIdx: Int) {
        TODO("Not yet implemented")
    }

    override fun beUpdate(title: String, category: ObCategory) {
        TODO("Not yet implemented")
    }

    override fun beSetEdit(category: ObCategory, page: ObPage) {
        TODO("Not yet implemented")
    }

    override fun bePickUnPick(item: ObMenuItem) {
        TODO("Not yet implemented")
    }

    override fun beUpload(complete: (Boolean) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun bePublish() {
        TODO("Not yet implemented")
    }

    override fun beLowerCurtain() {
        TODO("Not yet implemented")
    }
}