package com.cartoonhero.privatekitchen_android.stage.scenarios.storage

import com.cartoonhero.privatekitchen_android.actors.Transformer
import com.cartoonhero.privatekitchen_android.actors.archmage.Archmage
import com.cartoonhero.privatekitchen_android.actors.archmage.LiveScene
import com.cartoonhero.privatekitchen_android.actors.archmage.MassTeleport
import com.cartoonhero.privatekitchen_android.actors.archmage.Teleporter
import com.cartoonhero.privatekitchen_android.actors.objBox.ObDb
import com.cartoonhero.privatekitchen_android.props.entities.*
import com.cartoonhero.privatekitchen_android.props.obEntities.ObOption
import com.cartoonhero.privatekitchen_android.props.obEntities.ObOption_
import com.cartoonhero.privatekitchen_android.stage.scene.storage.CustomizeOptDirector
import com.cartoonhero.theatre.Courier
import com.cartoonhero.theatre.Scenario
import kotlinx.coroutines.*

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class CustomizeOptScenario: Scenario(), CustomizeOptDirector {
    private var sequence: Int = -1
    private var editType: EditType = EditType.Modify
    private lateinit var editOption: ObOption
    private val archmage: Archmage by lazy {
        Archmage(this)
    }

    private fun actShowTime(teleporter: Teleporter) {
        archmage.beSetWaypoint(teleporter)
    }
    private fun actCollectParcels() {
        launch {
            val pSet = Courier(this@CustomizeOptScenario).beClaim()
            for (parcel in pSet) {
                when(val content = parcel.content) {
                    is ObOption -> {
                        editOption = content
                        editType = if (content.id.equals(0)) {
                            EditType.Add
                        } else {
                            EditType.Modify
                        }
                        archmage.beChant(LiveScene(prop = editType))
                        archmage.beChant(LiveScene(prop = editOption))
                    }
                    is Int -> {
                        sequence = content
                    }
                }
            }
        }
    }
    private fun actUpdate(optFile: ObOptFile, remove: Boolean) {
        val getOptJob: Deferred<ObOption> = async {
            val tmpOpt = optFile.option
            tmpOpt.titleText = Transformer().beToJson(
                LocalizedText(local = optFile.titleText)
            )
            tmpOpt
        }
        if (remove) {
            editType = EditType.Remove
            archmage.beChant(LiveScene(prop = editType))
        }
        when(editType) {
            EditType.Add -> launch {
                val tmpOpt = getOptJob.await()
                prepareModify(tmpOpt, ModifyType.Create)
            }
            EditType.Modify -> launch {
                val tmpOpt = getOptJob.await()
                tmpOpt.id = editOption.id
                prepareModify(tmpOpt, ModifyType.Update)
            }
            EditType.Remove -> launch {
                val tmpOpt = getOptJob.await()
                val optBox = ObDb().beTakeBox(ObOption::class.java)
                optBox.remove(tmpOpt.id)
                val action = ModifyOptionAction(ModifyType.Delete, tmpOpt)
                archmage.beChant(MassTeleport(stuff = action))
            }
        }
    }
    private fun actLowerCurtain() {
        archmage.beShutOff()
    }

    /** --------------------------------------------------------------------------------------------------- **/

    private suspend fun prepareModify(opt: ObOption, type: ModifyType) {
        val oBox = ObDb().beTakeBox(ObOption::class.java)
        oBox.put(opt)
        val query = oBox.query(
            ObOption_.spotId.equal(opt.spotId)
        ).build()
        val found = query.findUnique()
        if (found != null) {
            val action = ModifyOptionAction(type, found)
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

    override fun beUpdate(optFile: ObOptFile, remove: Boolean) {
        tell { actUpdate(optFile, remove) }
    }

    override fun beLowerCurtain() {
        tell { actLowerCurtain() }
    }
}