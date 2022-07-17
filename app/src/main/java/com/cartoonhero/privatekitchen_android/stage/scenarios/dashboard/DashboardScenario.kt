package com.cartoonhero.privatekitchen_android.stage.scenarios.dashboard

import android.content.Context
import com.apollographql.apollo3.api.Optional
import com.cartoonhero.privatekitchen_android.actors.Transformer
import com.cartoonhero.privatekitchen_android.actors.apolloApi.ApiStatus
import com.cartoonhero.privatekitchen_android.actors.apolloApi.Helios
import com.cartoonhero.privatekitchen_android.actors.archmage.*
import com.cartoonhero.privatekitchen_android.actors.objBox.ObDb
import com.cartoonhero.privatekitchen_android.actors.timeGuardian.TimeGuardian
import com.cartoonhero.privatekitchen_android.actors.timeGuardian.formatYMdHm
import com.cartoonhero.privatekitchen_android.patterns.obTransfer.OrderTransfer
import com.cartoonhero.privatekitchen_android.props.ShopOpenedEvent
import com.cartoonhero.privatekitchen_android.props.entities.Dashboard
import com.cartoonhero.privatekitchen_android.props.entities.FlowStep
import com.cartoonhero.privatekitchen_android.props.entities.OrderForm
import com.cartoonhero.privatekitchen_android.props.entities.Workflow
import com.cartoonhero.privatekitchen_android.props.inlineTools.applyEdit
import com.cartoonhero.privatekitchen_android.props.mainContext
import com.cartoonhero.privatekitchen_android.props.obEntities.*
import com.cartoonhero.privatekitchen_android.props.sharedStorage
import com.cartoonhero.privatekitchen_android.stage.scene.dashboard.DashboardDirector
import com.cartoonhero.theatre.Courier
import com.cartoonhero.theatre.Scenario
import graphqlApollo.operation.FindDashboardQuery
import graphqlApollo.operation.FindOrderFormQuery
import graphqlApollo.operation.SearchMatchTimeOrdersQuery
import graphqlApollo.operation.type.QueryOrder
import io.objectbox.kotlin.and
import kotlinx.coroutines.*
import java.util.*

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class DashboardScenario : Scenario(), DashboardDirector {
    private val archmage: Archmage by lazy {
        Archmage(this)
    }
    private val openTimeUDKey = "UtcOpenTime"
    private val shopIncome: String = "ShopIncome"
    private var station: ObWorkstation? = null
    private var stationId: String = ""
    var dashboard: Dashboard? = null

    private fun actShowTime(teleport: Teleporter) {
        archmage.beSetWaypoint(teleport)
        archmage.beSetTeleportation(teleportation)
    }

    private fun actCollectParcels(complete: (() -> Unit)?) {
        val collectJob: Deferred<Boolean> = async {
            val pSet = Courier(this@DashboardScenario).beClaim()
            for (parcel in pSet) {
                when (val content = parcel.content) {
                    is ObWorkstation -> {
                        stationId = content.uniqueId ?: ""
                        station = content
                        withContext(Dispatchers.Main) {
                            complete?.let { it() }
                        }
                    }
                }
            }
            true
        }
        launch {
            if (collectJob.await()) {
                syncDashboard()
            }
        }
    }

    private fun actLocal(
        utcText: String, idx: Int,
        format: String, complete: (Int, String) -> Unit
    ) {
        launch {
            val localDate: String = TimeGuardian().beTextToText(
                utcText, format,
                TimeZone.getTimeZone("UTC"),
                TimeZone.getDefault()
            )
            withContext(Dispatchers.Main) {
                complete(idx, localDate)
            }
        }
    }

    private fun actLookOver(selected: Int, flow: FlowStep, complete: () -> Unit) {
        val recipient = "LookOverScenario"
        launch {
            val courier = Courier(this@DashboardScenario)
            val totalSteps: Int = dashboard?.steps?.size ?: 0
            val numbers: Map<String, Int> = mapOf(
                ("selectedIdx" to selected),
                ("totalSteps" to totalSteps)
            )
            courier.beApply(numbers, recipient)
            courier.beApply(flow, recipient)
            withContext(Dispatchers.Main) {
                complete()
            }
        }
    }

    private fun actPanel(action: Int, complete: (() -> Unit)?) {
        val recipient: String = "BoardPanelScenario"
        val courier = Courier(this@DashboardScenario)
        when (action) {
            0 -> launch {
                if (!shopOpened()) {
                    courier.beApply(action, recipient)
                    courier.beApply(station, recipient)
                    withContext(Dispatchers.Main) {
                        complete?.let { it() }
                    }
                }
            }
            1 -> launch {
                courier.beApply(action, recipient)
                courier.beApply(dashboard, recipient)
                withContext(Dispatchers.Main) {
                    complete?.let { it() }
                }
            }
        }
    }

    private fun actWindUp() {
        shopWindUp()
    }

    private fun actLowerCurtain() {
        archmage.beShutOff()
    }

    /** ----------------------------------------------------------------------------------------------------- **/

    private suspend fun syncDashboard() {
        val dBox = ObDb().beTakeBox(ObDashboard::class.java)
        val query = dBox.query(
            ObDashboard_.ownerId.equal(stationId)
        ).build()
        val found = query.findUnique()
        if (found == null) {
            callFindBoardApi()
        } else {
            parseObBoard(found)
        }
    }

    private fun callFindBoardApi() {
        Helios(this).beFindDashboard(stationId) { status, respObj ->
            when (status) {
                ApiStatus.SUCCESS -> {
                    if (respObj != null) {
                        launch {
                            val newBoard = Transformer().beTransfer<
                                    FindDashboardQuery.FindDashboard, Dashboard>(respObj)
                            if (newBoard != null)
                                archmage.beChant(MassTeleport(newBoard))
                        }
                    }
                }
                ApiStatus.FAILED -> print("失敗")
            }
        }
    }

    private fun parseObBoard(board: ObDashboard) {
        val parseJob: Deferred<Dashboard> = async {
            val newBoard = Dashboard(
                ownerId = board.ownerId, closeTime = board.closeTime,
                steps = listOf()
            )
            val steps = mutableListOf<Workflow>()
            for (flow in board.steps) {
                steps.add(
                    Workflow(step = flow.step, name = flow.name)
                )
            }
            newBoard.steps = steps
            newBoard
        }
        launch {
            val aBoard = parseJob.await()
            archmage.beChant(MassTeleport(aBoard))
        }
    }

    private fun queryAllOrders() {
        val queryJob: Deferred<Map<Int, List<OrderForm>>> = async {
            val result: MutableMap<Int, List<OrderForm>> = mutableMapOf()
            val stepCount: Int = dashboard?.steps?.size ?: 0
            val fBox = ObDb().beTakeBox(ObOrderForm::class.java)
            for (i in 0 until stepCount) {
                val query = fBox.query(
                    ObOrderForm_.uniqueId.equal(stationId)
                            and ObOrderForm_.state.equal(i)
                ).build()
                val foundData = query.find()
                result[i] = OrderTransfer(this@DashboardScenario)
                    .beTransfer(foundData)
            }
            result
        }
        launch {
            val finalResult = queryJob.await()
            archmage.beChant(LiveScene(finalResult))
        }
    }

    private suspend fun syncForms(state: Int, complete: (List<OrderForm>) -> Unit) {
        val sharedPrefs = mainContext.getSharedPreferences(
            sharedStorage, Context.MODE_PRIVATE
        )
        val openDate: String = sharedPrefs.getString(openTimeUDKey, "") ?: ""
        val open: Date = TimeGuardian().beTextTo(openDate, formatYMdHm, TimeZone.getDefault())
        val close: Date = TimeGuardian().beTextTo(
            dashboard?.closeTime ?: "",
            formatYMdHm, TimeZone.getDefault()
        )
        if (close > Date()) {
            val aQuery = QueryOrder(
                kitchenId = Optional.presentIfNotNull(station?.kitchen_id),
                state = Optional.presentIfNotNull(state)
            )
            Helios(this).beSearchMatchTimeOrders(open, close, aQuery) { status, respData ->
                when (status) {
                    ApiStatus.SUCCESS -> launch {
                        if (respData != null) {
                            val formList: List<OrderForm> = Transformer().beListTo<
                                    SearchMatchTimeOrdersQuery.SearchMatchTimeOrder?,
                                    OrderForm>(respData) ?: listOf()
                            val forms: MutableList<OrderForm> = formList.toMutableList()
                            forms.removeIf { it.arrival == null }
                            tell { complete(forms) }
                            val transfer = OrderTransfer(this@DashboardScenario)
                            transfer.beTransfer(state, forms)
                        }
                    }
                    ApiStatus.FAILED -> print("sync failed")
                }
            }
        }
    }

    private suspend fun shopOpened(): Boolean {
        if (dashboard != null) {
            val close: Date = TimeGuardian().beTextTo(
                dashboard!!.closeTime ?: "",
                formatYMdHm, TimeZone.getDefault()
            )
            if (close > Date()) {
                archmage.beChant(LiveScene(prop = ShopOpenedEvent(true)))
                return true
            }
        }
        return false
    }

    private suspend fun syncNewOrders() {
        syncForms(1) {
            val result: Map<Int, List<OrderForm>> = mapOf(1 to it)
            archmage.beChant(LiveScene(result))
        }
    }

    private fun getOrderDetail(form: OrderForm, complete: (OrderForm) -> Unit) {
        Helios(this).beFindOrderForm(form.uniqueId ?: "") { status, respObj ->
            when (status) {
                ApiStatus.SUCCESS -> launch {
                    val newForm = Transformer().beTransfer<
                            FindOrderFormQuery.FindOrderForm?,
                            OrderForm>(respObj)
                    if (newForm != null) {
                        val transfer = OrderTransfer(this@DashboardScenario)
                        if (transfer.beGetOrders(newForm) != null) {
                            tell { complete(newForm) }
                        }
                    }
                }
                ApiStatus.FAILED -> print("Not found")
            }
        }
    }

    private fun shopWindUp() {
        val sharedPrefs = mainContext.getSharedPreferences(
            sharedStorage, Context.MODE_PRIVATE
        )
        val incomeValue: Float = sharedPrefs.getFloat(shopIncome, 0.0F)
        if (incomeValue != 0.0F) {
            archmage.beChant(LiveScene(("incomeValue" to incomeValue)))
            return
        }
        launch {
            if (!shopOpened()) {
                val openDate: String = sharedPrefs.getString(openTimeUDKey, "") ?: ""
                val open: Date = TimeGuardian().beTextTo(
                    openDate, formatYMdHm, TimeZone.getDefault()
                )
                val close: Date = TimeGuardian().beTextTo(
                    dashboard?.closeTime ?: "",
                    formatYMdHm, TimeZone.getDefault()
                )
                if (close > Date()) {
                    calculateIncome(open, close)
                }
            }
        }
    }

    private fun calculateIncome(open: Date, close: Date) {
        val aQuery = QueryOrder(
            kitchenId = Optional.presentIfNotNull(station?.kitchen_id),
            paid = Optional.presentIfNotNull(true)
        )
        Helios(this).beSearchMatchTimeOrders(open, close, aQuery) { status, respData ->
            when (status) {
                ApiStatus.SUCCESS -> {
                    if (respData != null) {
                        val parseJob: Deferred<List<OrderForm>> = async {
                            val forms: List<OrderForm> = Transformer().beListTo<
                                    SearchMatchTimeOrdersQuery.SearchMatchTimeOrder?,
                                    OrderForm>(respData) ?: listOf()
                            forms
                        }
                        launch {
                            val transfer = OrderTransfer(this@DashboardScenario)
                            transfer.beTransfer(0, parseJob.await())
                        }
                        val sumJob: Deferred<Double> = async {
                            var total = 0.0
                            val forms: List<OrderForm> = parseJob.await()
                            for (form in forms) {
                                total += form.info?.cost ?: 0.0
                            }
                            total
                        }
                        launch {
                            val result = sumJob.await()
                            archmage.beChant(LiveScene(("WindUpEvent" to result)))
                            val sharedPrefs = mainContext.getSharedPreferences(
                                sharedStorage, Context.MODE_PRIVATE
                            )
                            sharedPrefs.applyEdit {
                                putFloat(shopIncome, result.toFloat())
                            }
                            OrderTransfer(this@DashboardScenario).beCleanAll()
                        }
                    }
                }
                ApiStatus.FAILED -> print("error")
            }
        }
    }

    private val teleportation: Teleporter = object : Teleporter {
        override fun beSpellCraft(spell: Spell) {
            if (spell is MassTeleport) {
                when (val cargo = spell.cargo) {
                    is Dashboard -> {
                        dashboard = cargo
                        buildFlowGridVM(cargo)
                    }
                }
            }
        }
    }

    private fun buildFlowGridVM(board: Dashboard) {
    }


    /** ----------------------------------------------------------------------------------------------------- **/

    override fun beShowTime(teleport: Teleporter) {
        tell { actShowTime(teleport) }
    }

    override fun beCollectParcels(complete: (() -> Unit)?) {
        tell { actCollectParcels(complete) }
    }

    override fun beLocal(
        utcText: String, idx: Int,
        format: String, complete: (Int, String) -> Unit
    ) {
        tell { actLocal(utcText, idx, format, complete) }
    }

    override fun beLookOver(selected: Int, flow: FlowStep, complete: () -> Unit) {
        tell { actLookOver(selected, flow, complete) }
    }

    override fun bePanel(action: Int, complete: (() -> Unit)?) {
        tell { actPanel(action, complete) }
    }

    override fun beWindUp() {
        tell { actWindUp() }
    }

    override fun beLowerCurtain() {
        tell { actLowerCurtain() }
    }

}