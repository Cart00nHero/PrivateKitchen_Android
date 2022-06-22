package com.cartoonhero.privatekitchen_android.patterns.obTransfer

import com.cartoonhero.privatekitchen_android.actors.Transformer
import com.cartoonhero.privatekitchen_android.actors.generator.Generator
import com.cartoonhero.privatekitchen_android.actors.objBox.ObDb
import com.cartoonhero.privatekitchen_android.props.entities.DiningWay
import com.cartoonhero.privatekitchen_android.props.entities.MenuItem
import com.cartoonhero.privatekitchen_android.props.entities.OrderForm
import com.cartoonhero.privatekitchen_android.props.entities.OrderItem
import com.cartoonhero.privatekitchen_android.props.obEntities.*
import com.cartoonhero.theatre.Pattern
import com.cartoonhero.theatre.Scenario
import kotlinx.coroutines.*

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class OrderTransfer(attach: Scenario) : Pattern(attach) {

    private fun actTransfer(obForms: List<ObOrderForm>, myJob: CompletableDeferred<List<OrderForm>>) {
        launch {
            val forms: MutableList<OrderForm> = mutableListOf()
            for (obFrom in obForms) {
                val newForm = OrderForm(
                    uniqueId = obFrom.uniqueId,
                    kitchenId = obFrom.kitchenId,
                    userId = obFrom.userId, setUp = obFrom.setUp,
                    state = obFrom.state, arrival = obFrom.arrival,
                    orderer = obFrom.beOrderer(), info = obFrom.beInfo(),
                    items = listOf(), diningWay = null
                )
                val obWay: ObDiningWay? = obFrom.diningWay.target
                if (obWay != null) {
                    val wSID: String = obWay.spotId.toString()
                    val dWay = DiningWay(
                        spotId = wSID, sequence = obWay.sequence,
                        option = obWay.beOption()
                    )
                    newForm.diningWay = dWay
                }
                forms.add(newForm)
            }
            myJob.complete(forms)
        }
    }
    private fun actTransfer(state: Int, forms: List<OrderForm>, myJob: CompletableDeferred<List<ObOrderForm>>) {
        launch {
            clean(state)
            val obForms: List<ObOrderForm> = parseForms(forms)
            val fBox = ObDb().beTakeBox(ObOrderForm::class.java)
            fBox.put(obForms)
            val query = fBox.query(
                ObOrderForm_.state.equal(state)
            ).build()
            myJob.complete(query.find())
        }
    }
    private fun actTransfer(odrForm: OrderForm, myJob: CompletableDeferred<ObOrderForm?>) {
        launch {
            val odrerJson = Transformer().beToJson(odrForm.orderer)
            val infoJson = Transformer().beToJson(odrForm.info)
            val newForm = ObOrderForm(
                id = 0, uniqueId = odrForm.uniqueId,
                kitchenId = odrForm.kitchenId,
                userId = odrForm.userId, setUp = odrForm.userId,
                state = odrForm.state, arrival = odrForm.arrival,
                orderer = odrerJson, info = infoJson
            )
            val fBox = ObDb().beTakeBox(ObOrderForm::class.java)
            val query = fBox.query(
                ObOrderForm_.uniqueId.equal(odrForm.uniqueId ?: "")
            ).build()
            val found = query.findUnique()
            if (found != null) {
                newForm.id = found.id
            } else {
                if (odrForm.diningWay != null) {
                    val obWay = findDiningWay(odrForm.diningWay!!)
                    if (obWay != null) newForm.diningWay.target = obWay
                }
            }
            fBox.put(newForm)
        }
    }
    private fun actGetOrders(form: OrderForm, myJob: CompletableDeferred<ObOrderForm?>) {
        launch {
            cleanOrders(form)
            val fBox = ObDb().beTakeBox(ObOrderForm::class.java)
            val query = fBox.query(
                ObOrderForm_.uniqueId.equal(form.uniqueId ?: "")
            ).build()
            val newForm = query.findUnique()
            if (newForm != null) {
                val orders = parseOrders(form.items ?: listOf(), newForm)
                newForm.items.addAll(orders)
                newForm.items.applyChangesToDb()
                fBox.put(newForm)
                myJob.complete(query.findUnique())
            }
        }
    }
    private fun actCleanAll() {
        launch {
            val fBox = ObDb().beTakeBox(ObOrderForm::class.java)
            val oBox = ObDb().beTakeBox(ObOrderItem::class.java)
            val cBox = ObDb().beTakeBox(ObChoice::class.java)
            cBox.removeAll()
            oBox.removeAll()
            fBox.removeAll()
        }
    }
    private suspend fun cleanOrders(form: OrderForm) {
        val fBox = ObDb().beTakeBox(ObOrderForm::class.java)
        val oBox = ObDb().beTakeBox(ObOrderItem::class.java)
        val cBox = ObDb().beTakeBox(ObChoice::class.java)
        val findJob: Deferred<ObOrderForm?> = async {
            val query = fBox.query(
                ObOrderForm_.uniqueId.equal(form.uniqueId ?: "")
            ).build()
            return@async query.findUnique()
        }
        val obForm = findJob.await()
        if (obForm != null) {
            val query = oBox.query(
                ObOrderItem_.toFormId.equal(obForm.id)
            ).build()
            val orders = query.find()
            for (order in orders) {
                val cQuery = cBox.query(
                    ObChoice_.toOrderId.equal(order.id)
                ).build()
                cBox.remove(cQuery.find())
            }
            oBox.remove(orders)
        }
    }
    private suspend fun clean(state: Int) {
        val aBox = ObDb().beTakeBox(ObOrderForm::class.java)
        val query = aBox.query(
            ObOrderForm_.state.equal(state)
        ).build()
        aBox.remove(query.find())
    }
    private suspend fun parseForms(forms: List<OrderForm>): List<ObOrderForm> {
        val obForms: MutableList<ObOrderForm> = mutableListOf()
        for (aForm in forms) {
            val infoJson = Transformer().beToJson(aForm.info)
            val odrerJson = Transformer().beToJson(aForm.orderer)
            val obForm = ObOrderForm(
                id = 0, uniqueId = aForm.uniqueId,
                kitchenId = aForm.kitchenId,
                userId = aForm.userId, setUp = aForm.userId,
                state = aForm.state, arrival = aForm.arrival,
                orderer = odrerJson, info = infoJson
            )
            if (aForm.diningWay != null) {
                val dWay = findDiningWay(aForm.diningWay!!)
                if (dWay != null) obForm.diningWay.target = dWay
            }
            obForms.add(obForm)
        }
        return obForms
    }
    private suspend fun parseOrders(odrItems: List<OrderItem>, form: ObOrderForm): List<ObOrderItem> {
        val obOrders: MutableList<ObOrderItem> = mutableListOf()
        for (order in odrItems) {
            val obOrder = ObOrderItem(
                id = 0, pagination = order.pagination,
                category_id = order.categoryId, quantity = order.quantity
            )
            obOrder.item.target = find(order.item)
            obOrder.toForm.setAndPutTarget(form)
            obOrders.add(obOrder)
        }
        val oBox = ObDb().beTakeBox(ObOrderItem::class.java)
        if (obOrders.size > 0) {
            oBox.put(obOrders)
        }
        val query = oBox.query(
            ObOrderItem_.toFormId.equal(form.id)
        ).build()
        return query.find()
    }
    private suspend fun findDiningWay(way: DiningWay): ObDiningWay? {
        val tempId = Generator().beSpotId()
        val spotId: Long = way.spotId?.toLong() ?: tempId
        val dBox = ObDb().beTakeBox(ObDiningWay::class.java)
        val query = dBox.query(
            ObDiningWay_.spotId.equal(spotId)
        ).build()
        return query.findUnique()
    }
    private suspend fun find(item: MenuItem): ObMenuItem? {
        val itBox = ObDb().beTakeBox(ObMenuItem::class.java)
        val tempId: Long = Generator().beSpotId()
        val spotId: Long = item.spotId?.toLong() ?: tempId
        val query = itBox.query(
            ObMenuItem_.spotId.equal(spotId)
        ).build()
        return query.findUnique()
    }


    suspend fun beTransfer(obForms: List<ObOrderForm>): List<OrderForm> {
        val actorJob = CompletableDeferred<List<OrderForm>>()
        tell { actTransfer(obForms, actorJob) }
        return actorJob.await()
    }
    suspend fun beTransfer(state: Int, forms: List<OrderForm>): List<ObOrderForm> {
        val actorJob = CompletableDeferred<List<ObOrderForm>>()
        tell { actTransfer(state, forms, actorJob) }
        return actorJob.await()
    }
    suspend fun beTransfer(odrForm: OrderForm): ObOrderForm? {
        val actorJob = CompletableDeferred<ObOrderForm?>()
        tell { actTransfer(odrForm, actorJob) }
        return actorJob.await()
    }
    suspend fun beGetOrders(form: OrderForm): ObOrderForm? {
        val actorJob = CompletableDeferred<ObOrderForm?>()
        tell { actGetOrders(form, actorJob) }
        return actorJob.await()
    }
    fun beCleanAll() {
        tell { actCleanAll() }
    }
}