package com.cartoonhero.privatekitchen_android.stage.scenarios.storage

import com.cartoonhero.privatekitchen_android.actors.Transformer
import com.cartoonhero.privatekitchen_android.actors.archmage.Archmage
import com.cartoonhero.privatekitchen_android.actors.archmage.LiveScene
import com.cartoonhero.privatekitchen_android.actors.archmage.MassTeleport
import com.cartoonhero.privatekitchen_android.actors.archmage.Teleporter
import com.cartoonhero.privatekitchen_android.actors.objBox.ObDb
import com.cartoonhero.privatekitchen_android.props.entities.*
import com.cartoonhero.privatekitchen_android.props.obEntities.ObMenuItem
import com.cartoonhero.privatekitchen_android.props.obEntities.ObMenuItem_
import com.cartoonhero.privatekitchen_android.stage.scene.storage.EditItemDirector
import com.cartoonhero.theatre.Courier
import com.cartoonhero.theatre.Scenario
import kotlinx.coroutines.*

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class EditItemScenario : Scenario(), EditItemDirector {
    private val archmage: Archmage by lazy {
        Archmage(this)
    }
    private lateinit var editItem: ObMenuItem
    private var editType: EditType = EditType.Modify

    private fun actShowTime(teleporter: Teleporter) {
        archmage.beSetWaypoint(teleporter)
    }

    private fun actCollectParcels() {
        launch {
            val pSet = Courier(this@EditItemScenario).beClaim()
            for (parcel in pSet) {
                when (val content = parcel.content) {
                    is ObMenuItem -> {
                        editItem = content
                        editType = if (content.id.equals(0)) {
                            EditType.Add
                        } else {
                            EditType.Modify
                        }
                        archmage.beChant(LiveScene(prop = editType))
                        archmage.beChant(LiveScene(prop = editItem))
                    }
                }
            }
        }
    }

    private fun actUpdate(itemFile: ObMuItemFile, remove: Boolean) {
        val itemTextJob: Deferred<ObMenuItem> = async {
            val tmpItem = itemFile.item
            tmpItem.nameText = Transformer().beToJson(LocalizedText(local = itemFile.nameText))
            tmpItem.introText = Transformer().beToJson(LocalizedText(local = itemFile.introText))
            tmpItem
        }
        if (remove) {
            editType = EditType.Remove
            archmage.beChant(LiveScene(prop = editType))
        }
        when (editType) {
            EditType.Add -> launch { prepareModify(itemTextJob.await(), ModifyType.Create) }
            EditType.Modify -> launch {
                val tmpItem = itemTextJob.await()
                tmpItem.id = editItem.id
                tmpItem.spotId = editItem.spotId
                prepareModify(itemTextJob.await(), ModifyType.Update)
            }
            EditType.Remove -> launch {
                val itemBox = ObDb().beTakeBox(ObMenuItem::class.java)
                itemBox.remove(editItem.id)
                val action = ModifyItemAction(ModifyType.Delete, editItem)
                archmage.beChant(MassTeleport(stuff = action))
            }
        }
    }

    private fun actLowerCurtain() {
        archmage.beShutOff()
    }

    /** ----------------------------------------------------------------------------------------------------- **/

    private suspend fun prepareModify(obItem: ObMenuItem, actionType: ModifyType) {
        val mBox = ObDb().beTakeBox(ObMenuItem::class.java)
        mBox.put(obItem)
        val query = mBox.query(
            ObMenuItem_.spotId.equal(obItem.spotId)
        ).build()
        val found = query.findUnique()
        if (found != null) {
            val action = ModifyItemAction(actionType, found)
            archmage.beChant(MassTeleport(stuff = action))
        }
    }

    /** --------------------------------------------------------------------------------------------------- **/

    override fun beShowTime(teleporter: Teleporter) {
        tell { actShowTime(teleporter) }
    }

    override fun beCollectParcels() {
        tell { actCollectParcels() }
    }

    override fun beUpdate(itemFile: ObMuItemFile, remove: Boolean) {
        tell { actUpdate(itemFile, remove) }
    }

    override fun beLowerCurtain() {
        tell { actLowerCurtain() }
    }
}