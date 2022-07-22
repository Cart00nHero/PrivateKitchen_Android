package com.cartoonhero.privatekitchen_android.stage.scenarios.dashboard

import com.cartoonhero.privatekitchen_android.actors.Transformer
import com.cartoonhero.privatekitchen_android.actors.apolloApi.ApiStatus
import com.cartoonhero.privatekitchen_android.actors.apolloApi.Helios
import com.cartoonhero.privatekitchen_android.actors.archmage.Archmage
import com.cartoonhero.privatekitchen_android.actors.archmage.LiveScene
import com.cartoonhero.privatekitchen_android.actors.archmage.Teleporter
import com.cartoonhero.privatekitchen_android.actors.objBox.ObDb
import com.cartoonhero.privatekitchen_android.actors.timeGuardian.TimeGuardian
import com.cartoonhero.privatekitchen_android.actors.timeGuardian.formatHm
import com.cartoonhero.privatekitchen_android.patterns.obTransfer.OrderTransfer
import com.cartoonhero.privatekitchen_android.props.entities.*
import com.cartoonhero.privatekitchen_android.props.obEntities.ObOrderForm
import com.cartoonhero.privatekitchen_android.props.obEntities.ObOrderForm_
import com.cartoonhero.privatekitchen_android.stage.scene.dashboard.LookOverDirector
import com.cartoonhero.theatre.Courier
import com.cartoonhero.theatre.Scenario
import graphqlApollo.operation.FindOrderFormQuery
import kotlinx.coroutines.*
import java.util.*

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class LookOverScenario: Scenario(), LookOverDirector {
    private val archmage: Archmage by lazy {
        Archmage(this)
    }
    private lateinit var nowFlow: FlowStep
    private var currentIdx: Int = 0
    private var totalSteps: Int = 0

    private fun actShowTime(teleport: Teleporter) {
        archmage.beSetWaypoint(teleport)
    }
    private fun actCollectParcels() {
        val collectJob: Deferred<Boolean> = async {
            val pSet = Courier(this@LookOverScenario).beClaim()
            for (parcel in pSet) {
                when(val content = parcel.content) {
                    is Map<*,*> -> {
                        content.forEach { key, value ->
                            if (value is Int) {
                                when(key) {
                                    "totalSteps" -> totalSteps = value
                                    "selectedIdx" -> currentIdx = value
                                }
                            }
                        }
                    }
                    is FlowStep -> {
                        nowFlow = content
                    }
                }
            }
            true
        }
        launch {
            if (collectJob.await()) {
                if (currentIdx < nowFlow.odrForms.size) {
                    val orderForm = nowFlow.odrForms[currentIdx]
                    handleDisplay(orderForm)
                }
            }
        }
    }

    private fun actParseInfo(form: ObOrderForm, complete: (Orderer, OrderInfo) -> Unit) {
        val odrerJson: String = form.orderer
        val infoJson: String = form.info
        launch {
            val odrer: Orderer = Transformer().beJsonTo<Orderer>(odrerJson) ?: Orderer("", "")
            val odrInfo: OrderInfo = Transformer().beJsonTo(infoJson) ?: OrderInfo()
            withContext(Dispatchers.Main) {
                complete(odrer, odrInfo)
            }
        }
    }
    private fun actGetLocalArrival(time: String, complete: (String) -> Unit) {
        launch {
            val localTime: String = TimeGuardian().beTextToText(
                time, formatHm, TimeZone.getTimeZone("UTC"),
                TimeZone.getDefault()
            )
            withContext(Dispatchers.Main) {
                complete(localTime)
            }
        }
    }
    private fun actGetDiningWay(form: ObOrderForm, complete: (LocalizedText) -> Unit) {
        launch {
            val json: String = form.diningWay.target.optionText
            val wayText = Transformer().beJsonTo<LocalizedText>(json)
            if (wayText != null) {
                withContext(Dispatchers.Main) {
                    complete(wayText)
                }
            }
        }
    }
    private fun actNext() {
        launch {
            val total: Int = nowFlow.odrForms.size
            if (total - currentIdx == 1) {
                currentIdx = 0
            } else {
                currentIdx += 1
            }
            handleDisplay(nowFlow.odrForms[currentIdx])
        }
    }
    private fun actPrevious() {
        launch {
            val total: Int = nowFlow.odrForms.size
            if (currentIdx == 0) {
                currentIdx = total - 1
            } else {
                currentIdx -= 1
            }
            handleDisplay(nowFlow.odrForms[currentIdx])
        }
    }

    /** ----------------------------------------------------------------------------------------------------- **/

    private suspend fun handleDisplay(form: OrderForm) {
        val oBox = ObDb().beTakeBox(ObOrderForm::class.java)
        val query = oBox.query(
            ObOrderForm_.uniqueId.equal(form.uniqueId ?: "")
        ).build()
        val found = query.findUnique()
        if (found != null) {
            if (found.items.size == 0) {
                getFormOrders(found)
            } else {
                archmage.beChant(LiveScene(found))
            }
        }
    }
    private fun getFormOrders(form: ObOrderForm) {
        Helios(this).beFindOrderForm(form.uniqueId ?: "") { status, respObj ->
            when(status) {
                ApiStatus.SUCCESS -> launch {
                    if (respObj != null) {
                        val aForm = Transformer().beTransfer<
                                FindOrderFormQuery.FindOrderForm, OrderForm>(respObj)
                        if (aForm != null) {
                            val transfer = OrderTransfer(this@LookOverScenario)
                            val obForm = transfer.beGetOrders(aForm)
                            if (obForm != null) {
                                archmage.beChant(LiveScene(obForm))
                            }
                        }
                    }
                }
                ApiStatus.FAILED -> print("爆")
            }
        }
    }
    private fun actMoveForm(toState: Int) {
        val odrForm = nowFlow.odrForms[currentIdx]
        if (odrForm.state == toState) return
        val removeJob: Deferred<ObOrderForm?> = async {
            var result: ObOrderForm? = null
            val fBox = ObDb().beTakeBox(ObOrderForm::class.java)
            val query = fBox.query(
                ObOrderForm_.uniqueId.equal(odrForm.uniqueId ?: "")
            ).build()
            val found = query.findUnique()
            if (found != null) {
                found.state = toState
                fBox.put(found)
                result = found
                val tempList: MutableList<OrderForm> = mutableListOf()
                tempList.addAll(nowFlow.odrForms)
                tempList.removeAt(currentIdx)
                nowFlow.odrForms = tempList
            }
            result
        }
        launch {
            removeJob.await()
            if (currentIdx < nowFlow.odrForms.size) {
                val newDisplay = nowFlow.odrForms[currentIdx]
                handleDisplay(newDisplay)
            }
        }
        launch {
            val newForm = removeJob.await()
            if (newForm != null) {
                print("OK")
                // TODO: Move
            }
            val record = FormMoveRecord(nowFlow, toState)
            archmage.beChant(
                LiveScene(
                    mapOf("Record" to record, "OrderForm" to newForm)
                )
            )
        }
    }

    private fun actLowerCurtain() {
        archmage.beShutOff()
    }

    /** ----------------------------------------------------------------------------------------------------- **/

    override fun beShowTime(teleport: Teleporter) {
        tell { actShowTime(teleport) }
    }

    override fun beCollectParcels() {
        tell { actCollectParcels() }
    }

    override fun beParseInfo(form: ObOrderForm, complete: (Orderer, OrderInfo) -> Unit) {
        tell { actParseInfo(form, complete) }
    }

    override fun beGetLocalArrival(time: String, complete: (String) -> Unit) {
        tell { actGetLocalArrival(time, complete) }
    }

    override fun beGetDiningWay(form: ObOrderForm, complete: (LocalizedText) -> Unit) {
        tell { actGetDiningWay(form, complete) }
    }

    override fun beNext() {
        tell { actNext() }
    }

    override fun bePrevious() {
        tell { actPrevious() }
    }

    override fun beMoveForm(toState: Int) {
        tell { actMoveForm(toState) }
    }

    override fun beBuildMenuSource(complete: (List<Int>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun beLowerCurtain() {
        tell { actLowerCurtain() }
    }
}