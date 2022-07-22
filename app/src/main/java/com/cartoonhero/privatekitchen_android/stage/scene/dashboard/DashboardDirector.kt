package com.cartoonhero.privatekitchen_android.stage.scene.dashboard

import com.cartoonhero.privatekitchen_android.actors.archmage.Teleporter
import com.cartoonhero.privatekitchen_android.props.entities.FlowStep
import com.cartoonhero.privatekitchen_android.props.entities.LocalizedText
import com.cartoonhero.privatekitchen_android.props.entities.OrderInfo
import com.cartoonhero.privatekitchen_android.props.entities.Orderer
import com.cartoonhero.privatekitchen_android.props.obEntities.ObOrderForm
import com.cartoonhero.privatekitchen_android.props.obEntities.ObWorkflow
import java.util.*

interface DashboardDirector {
    fun beShowTime(teleport: Teleporter)
    fun beCollectParcels(complete: (() -> Unit)?)
    fun beLocal(
        utcText: String, idx: Int, format: String,
        complete: (Int, String) -> Unit
    )
    fun beLookOver(
        selected: Int, flow: FlowStep, complete: () -> Unit
    )
    fun bePanel(action: Int, complete: (() -> Unit)?)
    fun beWindUp()
    fun beLowerCurtain()
}

interface BoardPanelDirector {
    fun beShowTime(teleport: Teleporter)
    fun beCollect()
    fun beParseCloseDate(close: Date, complete: (String) -> Unit)
    fun beSaveFlows(flows: List<ObWorkflow>, complete: (() -> Unit)?)
    fun beTomorrow(complete: (Date) -> Unit)
    fun beSetCloseTime(
        closeTime: String, recall: Int,
        complete: ((List<Boolean>, String) -> Void)?
    )
}

interface LookOverDirector {
    fun beShowTime(teleport: Teleporter)
    fun beCollectParcels()
    fun beParseInfo(
        form: ObOrderForm,
        complete: (Orderer, OrderInfo) -> Unit
    )

    fun beGetLocalArrival(
        time: String, complete: (String) -> Unit
    )
    fun beGetDiningWay(
        form: ObOrderForm,
        complete: (LocalizedText) -> Unit
    )
    fun beNext()
    fun bePrevious()
    fun beMoveForm(toState: Int)
    fun beBuildMenuSource(complete: (List<Int>) -> Unit)
    fun beLowerCurtain()
}