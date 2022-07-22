package com.cartoonhero.privatekitchen_android.patterns.obTransfer

import com.cartoonhero.privatekitchen_android.actors.objBox.ObDb
import com.cartoonhero.privatekitchen_android.props.entities.Dashboard
import com.cartoonhero.privatekitchen_android.props.entities.Workflow
import com.cartoonhero.privatekitchen_android.props.obEntities.ObDashboard
import com.cartoonhero.privatekitchen_android.props.obEntities.ObWorkflow
import com.cartoonhero.theatre.Pattern
import com.cartoonhero.theatre.Scenario
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class BoardTransfer(attach: Scenario) : Pattern(attach) {
    private lateinit var fromObj: Dashboard
    private lateinit var toObj: ObDashboard

    private fun actSet(from: Dashboard) {
        fromObj = from
        toObj = ObDashboard(id = 0, ownerId = from.ownerId)
    }
    private fun actTransfer(myJob: CompletableDeferred<ObDashboard?>) {
        launch {
            cleanDB()
            myJob.complete(startTransfer())
        }
    }
    private suspend fun cleanDB() {
        val bBox = ObDb().beTakeBox(ObDashboard::class.java)
        val flowBox = ObDb().beTakeBox(ObWorkflow::class.java)
        flowBox.removeAll()
        bBox.removeAll()
    }
    private suspend fun startTransfer(): ObDashboard? {
        val flows: List<Workflow> = fromObj.steps ?: listOf()
        val  obFlows: MutableList<ObWorkflow> = mutableListOf()
        for (i in flows.indices) {
            val flow = flows[i]
            val obFlow = ObWorkflow(id = 0, step = flow.step, name = flow.name)
            obFlows.add(obFlow)
        }
        val bBox = ObDb().beTakeBox(ObDashboard::class.java)
        val flowBox = ObDb().beTakeBox(ObWorkflow::class.java)
        flowBox.put(obFlows)
        val newFlows = flowBox.all
        toObj.steps.addAll(newFlows)
        toObj.steps.applyChangesToDb()
        bBox.put(toObj)
        return bBox.get(toObj.id)
    }
    fun beSet(from: Dashboard) {
        tell { actSet(from) }
    }
    suspend fun beTransfer(): ObDashboard? {
        val actorJob = CompletableDeferred<ObDashboard?>()
        tell { actTransfer(actorJob) }
        return actorJob.await()
    }
}