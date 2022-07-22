package com.cartoonhero.privatekitchen_android.stage.scenarios.dashboard

import android.content.Context
import com.apollographql.apollo3.api.Optional
import com.cartoonhero.privatekitchen_android.actors.apolloApi.ApiStatus
import com.cartoonhero.privatekitchen_android.actors.apolloApi.Helios
import com.cartoonhero.privatekitchen_android.actors.apolloApi.Icarus
import com.cartoonhero.privatekitchen_android.actors.archmage.Archmage
import com.cartoonhero.privatekitchen_android.actors.archmage.LiveScene
import com.cartoonhero.privatekitchen_android.actors.archmage.Teleporter
import com.cartoonhero.privatekitchen_android.actors.objBox.ObDb
import com.cartoonhero.privatekitchen_android.actors.timeGuardian.TimeGuardian
import com.cartoonhero.privatekitchen_android.actors.timeGuardian.formatISO8601
import com.cartoonhero.privatekitchen_android.actors.timeGuardian.formatYMdHm
import com.cartoonhero.privatekitchen_android.patterns.obTransfer.BoardTransfer
import com.cartoonhero.privatekitchen_android.props.entities.Dashboard
import com.cartoonhero.privatekitchen_android.props.inlineTools.applyEdit
import com.cartoonhero.privatekitchen_android.props.mainContext
import com.cartoonhero.privatekitchen_android.props.obEntities.ObDashboard
import com.cartoonhero.privatekitchen_android.props.obEntities.ObDashboard_
import com.cartoonhero.privatekitchen_android.props.obEntities.ObWorkflow
import com.cartoonhero.privatekitchen_android.props.obEntities.ObWorkstation
import com.cartoonhero.privatekitchen_android.props.sharedStorage
import com.cartoonhero.privatekitchen_android.stage.scene.dashboard.BoardPanelDirector
import com.cartoonhero.theatre.Courier
import com.cartoonhero.theatre.Scenario
import graphqlApollo.client.type.ModifyMenu
import graphqlApollo.operation.type.InputDashboard
import graphqlApollo.operation.type.InputWorkflow
import kotlinx.coroutines.*
import java.util.*

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class BoardPanelScenario: Scenario(), BoardPanelDirector {
    private lateinit var station: ObWorkstation
    private var ownerId: String = ""
    private val archmage: Archmage by lazy {
        Archmage(this)
    }

    private fun actShowTime(teleport: Teleporter) {
        archmage.beSetWaypoint(teleport)
    }
    private fun actCollect() {
        launch {
            val pSet = Courier(this@BoardPanelScenario).beClaim()
            for (parcel in pSet) {
                when(val content = parcel.content) {
                    is Int -> {
                        archmage.beChant(LiveScene(("Index" to content)))
                    }
                    is Dashboard -> launch {
                        val transfer = BoardTransfer(this@BoardPanelScenario)
                        transfer.beSet(content)
                        val newBoard = transfer.beTransfer()
                        if (newBoard != null) {
                            parseBoard(newBoard)
                        }
                    }
                }
            }
        }
    }

    private fun actParseCloseDate(close: Date, complete: (String) -> Unit) {
        launch {
            val dateText: String = TimeGuardian().beStraightTo(close, formatYMdHm)
            withContext(Dispatchers.Main) {
                complete(dateText)
            }
        }
    }

    private fun actSaveFlows(flows: List<ObWorkflow>, complete: (() -> Unit)?) {
        val inputJpb:Deferred<List<InputWorkflow>> = async {
            val stepInputs = mutableListOf<InputWorkflow>()
            for (i in flows.indices) {
                val flow = flows[i]
                stepInputs.add(
                    InputWorkflow(
                        name = Optional.presentIfNotNull(flow.name),
                        step = i
                    )
                )
            }
            stepInputs
        }
        launch {
            val inputDash = InputDashboard(steps = Optional.presentIfNotNull(inputJpb.await()))
            Helios(this@BoardPanelScenario).beUpdateDashboard(
                station.uniqueId ?: "", inputDash
            ) { status, _ ->
                when(status) {
                    ApiStatus.SUCCESS -> saveDashboard(flows, complete)
                    ApiStatus.FAILED -> print("Save error")
                }
            }
        }
    }

    private fun actTomorrow(complete: (Date) -> Unit) {
        launch {
            val endDate: Date = TimeGuardian().beTomorrow()
            withContext(Dispatchers.Main) {
                complete(endDate)
            }
        }
    }

    private fun actSetCloseTime(
        closeTime: String,
        recall: Int,
        complete: ((List<Boolean>, String) -> Void)?
    ) {
        val timeJob: Deferred<String> = async {
            val guardian = TimeGuardian()
            val closeUTC: String = guardian.beTextToText(
                closeTime, formatISO8601, fromTZ = TimeZone.getDefault(),
                toTZ = TimeZone.getTimeZone("UTC")
            )
            closeUTC
        }
        val dashJob: Deferred<Boolean> = async {
            val resultJob = CompletableDeferred<Boolean>()
            if (recall == 0 || recall == 1) {
                val closeUTC = timeJob.await()
                updateDashboard(closeUTC) {
                    resultJob.complete(it)
                }
            } else {
                resultJob.complete(true)
            }
            resultJob.await()
        }
        val menuJob: Deferred<Boolean> = async {
            val resultJob = CompletableDeferred<Boolean>()
            if (recall == 0 || recall == 2) {
                val closeUTC = timeJob.await()
                updateOnlineMenu(closeUTC) {
                    resultJob.complete(it)
                }
            } else {
                resultJob.complete(true)
            }
            resultJob.await()
        }
        launch {
            val result: List<Boolean> = listOf(dashJob.await(), menuJob.await())
            val closeUTC = timeJob.await()
            if (result[0] && result[1]) {
                val openDate: String = TimeGuardian().beDateToText(
                    Date(), formatISO8601, TimeZone.getDefault(),
                    TimeZone.getTimeZone("UTC")
                )
                val sharedPrefs = mainContext.getSharedPreferences(
                    sharedStorage, Context.MODE_PRIVATE
                )
                sharedPrefs.applyEdit {
                    putString("UtcOpenTime", openDate)
                    remove("ShopIncome")
                }
            }
            withContext(Dispatchers.Main) {
                complete?.let { it(result, closeUTC) }
            }
        }

    }

    /** ----------------------------------------------------------------------------------------------------- **/

    private fun updateDashboard(utcDate: String, complete: (Boolean) -> Unit) {
        val inputParam = InputDashboard(closeTime = Optional.presentIfNotNull(utcDate))
        Helios(this).beUpdateDashboard(
            station.uniqueId ?: "", inputParam
        ) { status, respObj ->
            when(status) {
                ApiStatus.SUCCESS -> {
                    if (respObj != null) {
                        complete(true)
                    } else {
                        complete(false)
                    }
                }
                ApiStatus.FAILED -> complete(false)
            }
        }
    }
    private fun updateOnlineMenu(utcDate: String, complete: (Boolean) -> Unit) {
        val kid: String = station.kitchen_id ?: ""
        Icarus(this).beUpdateMenu(
            kitchenId = kid,
            menu = ModifyMenu(deadline = Optional.presentIfNotNull(utcDate))
        ) { status, respObj ->
            when(status) {
                ApiStatus.SUCCESS -> {
                    if (respObj != null) {
                        complete(true)
                    } else {
                        complete(false)
                    }
                }
                ApiStatus.FAILED -> complete(false)
            }
        }
    }

    private fun saveDashboard(flows: List<ObWorkflow>, complete: (() -> Unit)?) {
        val newFlowJob: Deferred<List<ObWorkflow>> = async {
            val flowBox = ObDb().beTakeBox(ObWorkflow::class.java)
            flowBox.removeAll()
            flowBox.put(flows)
            flowBox.all
        }
        launch {
            val bBox = ObDb().beTakeBox(ObDashboard::class.java)
            val query = bBox.query(
                ObDashboard_.ownerId.equal(ownerId)
            ).build()
            val found = query.findUnique()
            if (found != null) {
                val newBoard: ObDashboard = found
                val newFlows = newFlowJob.await()
                newBoard.steps.addAll(newFlows)
                bBox.put(newBoard)
                withContext(Dispatchers.Main) {
                    complete?.let { it() }
                }
            }
        }
    }

    /** ----------------------------------------------------------------------------------------------------- **/

    private fun parseBoard(dashboard: ObDashboard) {
        // TODO: Parse VM
    }

    override fun beShowTime(teleport: Teleporter) {
        tell { actShowTime(teleport) }
    }

    override fun beCollect() {
        tell { actCollect() }
    }

    override fun beParseCloseDate(close: Date, complete: (String) -> Unit) {
        tell { actParseCloseDate(close, complete) }
    }

    override fun beSaveFlows(flows: List<ObWorkflow>, complete: (() -> Unit)?) {
        tell { actSaveFlows(flows, complete) }
    }

    override fun beTomorrow(complete: (Date) -> Unit) {
        tell { actTomorrow(complete) }
    }

    override fun beSetCloseTime(
        closeTime: String,
        recall: Int,
        complete: ((List<Boolean>, String) -> Void)?
    ) {
        tell { actSetCloseTime(closeTime, recall, complete) }
    }

}