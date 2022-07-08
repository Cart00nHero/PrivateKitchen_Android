package com.cartoonhero.privatekitchen_android.stage.scenarios.workstation

import com.cartoonhero.privatekitchen_android.actors.archmage.*
import com.cartoonhero.privatekitchen_android.props.ShowWorkstation
import com.cartoonhero.privatekitchen_android.props.entities.WkStTabScene
import com.cartoonhero.privatekitchen_android.props.entities.WorkSideMenuVM
import com.cartoonhero.privatekitchen_android.props.obEntities.ObWorkstation
import com.cartoonhero.privatekitchen_android.stage.scene.workstation.WorkStDirector
import com.cartoonhero.theatre.Courier
import com.cartoonhero.theatre.Scenario
import kotlinx.coroutines.*

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class WorkStScenario : Scenario(), WorkStDirector {
    private val archmage: Archmage by lazy {
        Archmage(this)
    }
    private var wkStation: ObWorkstation? = null

    private fun actShowTime(teleporter: Teleporter) {
        archmage.beSetTeleportation(this.teleportation)
        archmage.beSetWaypoint(teleporter)
    }

    private fun actPrepare(scene: WkStTabScene, complete: (() -> Unit)?) {
        if (wkStation == null) {
            return
        }
        when (scene) {
            WkStTabScene.Kitchen -> {
                launch {
                    Courier(this@WorkStScenario).beApply(
                        wkStation, "KitchenScenario"
                    )
                    withContext(Dispatchers.Main) {
                        complete?.let { it() }
                    }
                }
            }
            WkStTabScene.Storage ->
                launch {
                    Courier(this@WorkStScenario).beApply(
                        wkStation, "StorageScenario"
                    )
                    withContext(Dispatchers.Main) {
                        complete?.let { it() }
                    }
                }
            WkStTabScene.MenuEditor ->
                launch {
                    Courier(this@WorkStScenario).beApply(
                        wkStation, "EditMenuScenario"
                    )
                    withContext(Dispatchers.Main) {
                        complete?.let { it() }
                    }
                }
            WkStTabScene.Dashboard ->
                launch {
                    Courier(this@WorkStScenario).beApply(
                        wkStation, "EditMenuScenario"
                    )
                    withContext(Dispatchers.Main) {
                        complete?.let { it() }
                    }
                }
            else -> {}
        }
    }
    private fun actPackWkStParcel(recipient: String, complete: (() -> Unit)?) {
        if (wkStation != null) {
            launch {
                Courier(this@WorkStScenario).beApply(
                    wkStation, recipient
                )
                withContext(Dispatchers.Main) {
                    complete?.let { it() }
                }
            }
        }
    }
    private fun actLowerCurtain() {
        archmage.beShutOff()
    }

/** -------------------------------------------------------------------------------------------------------------- **/

    private fun buildSideMenuWith() {
        val source: MutableList<WorkSideMenuVM> = mutableListOf(
            WorkSideMenuVM(
                title = "My Kitchen",
                openScene = WkStTabScene.Kitchen
            ),
            WorkSideMenuVM(
                title = "Storage",
                openScene = WkStTabScene.Storage
            ),
            WorkSideMenuVM(
                title = "Menu Editor",
                openScene = WkStTabScene.MenuEditor
            ),
            WorkSideMenuVM(
                title = "Dashboard",
                openScene = WkStTabScene.Dashboard
            ),
            WorkSideMenuVM(
                title = "Order form",
                openScene = WkStTabScene.OrderForm
            )
        )
        if (wkStation!!.kitchen.target == null) {
            source.removeAt(2)
        }
        archmage.beChant(LiveScene(source.toList()))
    }

    private val teleportation: Teleporter = object : Teleporter {
        override fun beSpellCraft(spell: Spell) {
            if (spell is MassTeleport) {
                when (spell.cargo) {
                    is ShowWorkstation -> {
                        wkStation = spell.cargo.station
                        buildSideMenuWith()
                        launch {
                            Courier(this@WorkStScenario).beApply(
                                wkStation,"KitchenScenario"
                            )
                            archmage.beChant(
                                LiveScene(prop = WkStTabScene.Kitchen)
                            )
                        }
                    }
                }
            }
        }
    }

/** -------------------------------------------------------------------------------------------------------------- **/

    override fun beShowTime(teleporter: Teleporter) {
        tell { actShowTime(teleporter) }
    }

    override fun bePrepare(scene: WkStTabScene, complete: (() -> Unit)?) {
        tell { actPrepare(scene, complete) }
    }

    override fun bePackWkStParcel(recipient: String, complete: (() -> Unit)?) {
        tell { actPackWkStParcel(recipient, complete) }
    }

    override fun beLowerCurtain() {
        tell { actLowerCurtain() }
    }
}