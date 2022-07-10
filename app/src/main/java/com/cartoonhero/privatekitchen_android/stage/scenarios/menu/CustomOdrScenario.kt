package com.cartoonhero.privatekitchen_android.stage.scenarios.menu

import com.apollographql.apollo3.api.Optional
import com.cartoonhero.privatekitchen_android.actors.archmage.Archmage
import com.cartoonhero.privatekitchen_android.actors.archmage.MassTeleport
import com.cartoonhero.privatekitchen_android.props.entities.CalculateCustom
import com.cartoonhero.privatekitchen_android.props.entities.OrderStorage
import com.cartoonhero.privatekitchen_android.props.entities.UpdateTotalChosen
import com.cartoonhero.privatekitchen_android.stage.scene.menu.CustomOdrDirector
import com.cartoonhero.theatre.Courier
import com.cartoonhero.theatre.Scenario
import graphqlApollo.operation.type.InputChoice
import graphqlApollo.operation.type.InputOption
import graphqlApollo.operation.type.InputOrderItem
import kotlinx.coroutines.*

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class CustomOdrScenario : Scenario(), CustomOdrDirector {
    data class MyCargo(
        var odrItem: InputOrderItem? = null,
        var storage: OrderStorage? = null
    )

    private var itemIndex: Int = 0
    lateinit var odrItem: InputOrderItem
    lateinit var odrStore: OrderStorage
    private val archmage: Archmage by lazy {
        Archmage(this)
    }

    private fun actCollectParcels(complete: (InputOrderItem, Int) -> Unit) {
        val claimJob: Deferred<MyCargo> = async {
            val cargo = MyCargo()
            val pSet = Courier(this@CustomOdrScenario).beClaim()
            for (parcel in pSet) {
                when (val content = parcel.content) {
                    is Int -> itemIndex = content
                    is InputOrderItem -> {
                        odrItem = content
                        cargo.odrItem = content
                    }
                    is OrderStorage -> {
                        odrStore = content
                        cargo.storage = content
                    }
                }
            }
            cargo
        }
        launch {
            val result = claimJob.await()
            if (result.odrItem != null && result.storage != null) {
                val total: Int = result.storage!!.sumOfChosen[odrItem.item.spotId] ?: 0
                val remain: Int = result.odrItem!!.quantity - total
                withContext(Dispatchers.Main) {
                    complete(result.odrItem!!, remain)
                }
            }
        }
    }

    private fun actAddChoice(choice: InputChoice, complete: (() -> Unit)?) {
        val customize: List<InputChoice?> = odrItem.customize.getOrNull() ?: listOf()
        val newCustoms: MutableList<InputChoice?> = customize.toMutableList()
        newCustoms.add(choice)
        updateInputOrder(newCustoms)
        CoroutineScope(Dispatchers.Main).launch {
            complete?.let { it() }
        }
    }

    private fun actRemoveTab(
        tabIdx: Int, chosen: InputChoice,
        complete: ((idx: Int, amount: Int) -> Unit)?
    ) {
        val newChoices: MutableList<InputChoice?> = odrItem.customize
            .getOrNull()?.toMutableList() ?: mutableListOf()
        val totalCount: Int = newChoices.size
        if (tabIdx < totalCount) {
            launch {
                newChoices.removeAt(tabIdx)
                odrItem = updateInputOrder(newChoices)
            }
            var total: Int = odrStore.sumOfChosen[odrItem.item.spotId] ?: 0
            total -= chosen.amount
            odrStore.sumOfChosen[odrItem.item.spotId] = total
            CoroutineScope(Dispatchers.Main).launch {
                complete?.let { it(tabIdx, chosen.amount) }
            }
        }
    }

    private fun actPickOption(
        option: InputOption, choice: InputChoice,
        tabIdx: Int, complete: (InputChoice) -> Unit
    ) {
        val chosenOpts: MutableList<InputOption?> = choice.choices.toMutableList()
        val customize: MutableList<InputChoice?> = odrItem.customize
            .getOrNull()?.toMutableList() ?: mutableListOf()
        val isContain: Boolean = chosenOpts.find {
            it?.spotId?.toLongOrNull() == option.spotId.toLongOrNull()
        } != null
        if (!isContain) {
            val totalCount: Int = customize.size
            if (tabIdx < totalCount) {
                chosenOpts.add(option)
                val newChoice = updateChosenOptions(choice, chosenOpts)
                customize[tabIdx] = newChoice
                odrItem = updateInputOrder(customize)
                CoroutineScope(Dispatchers.Main).launch {
                    complete(newChoice)
                }
            }
        }
    }

    private fun actUnPickOption(
        option: InputOption, idx: Int, choice: InputChoice,
        tabIdx: Int, complete: (InputChoice, Int) -> Unit
    ) {
        val customize: MutableList<InputChoice?> = odrItem.customize
            .getOrNull()?.toMutableList() ?: mutableListOf()
        val picked: MutableList<InputOption?> = choice.choices.toMutableList()
        val isContain: Boolean = picked.find {
            it?.spotId?.toLongOrNull() == option.spotId.toLongOrNull()
        } != null
        if (isContain) {
            val totalCount: Int = picked.size
            if (tabIdx < totalCount) {
                picked.removeAt(idx)
                var newChoice = choice
                var addCount = 0
                if (picked.size == 0) {
                    addCount = choice.amount
                    newChoice = getNewChoice(0, choice.cost)
                }
                customize[tabIdx] = updateChosenOptions(newChoice, picked)
                odrItem = updateInputOrder(customize)
                CoroutineScope(Dispatchers.Main).launch {
                    complete(newChoice, addCount)
                }
            }
        }
    }

    private fun actOrderAmount(complete: (Int) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            complete(odrItem.quantity)
        }
    }

    /** ------------------------------------------------------------------------------------------------ **/

    private fun updateInputOrder(choices: List<InputChoice?>): InputOrderItem {
        return InputOrderItem(
            categoryId = odrItem.categoryId,
            customize = Optional.presentIfNotNull(choices),
            item = odrItem.item,
            pagination = odrItem.pagination,
            quantity = odrItem.quantity
        )
    }

    private fun getNewChoice(amount: Int, cost: Double): InputChoice {
        return InputChoice(
            amount = amount,
            choices = listOf(),
            cost = cost
        )
    }

    private fun updateChosenOptions(chosen: InputChoice, options: List<InputOption?>): InputChoice {
        return InputChoice(
            amount = chosen.amount,
            choices = options,
            cost = chosen.cost
        )
    }

    private fun actIncrease(
        value: Int, chosen: InputChoice,
        idx: Int, complete: (Int, InputChoice) -> Unit
    ) {
        val choice: InputChoice = getNewChoice((chosen.amount + value), chosen.cost)
        CoroutineScope(Dispatchers.Main).launch {
            complete(idx, choice)
        }
        val customize: MutableList<InputChoice?> = odrItem.customize
            .getOrNull()?.toMutableList() ?: mutableListOf()
        if (idx < customize.size) {
            customize[idx] = updateChosenOptions(choice, chosen.choices)
            odrItem = updateInputOrder(customize)
        }
    }

    private fun actCalculateChosen(chosen: InputChoice, tabIdx: Int) {
        val customize: MutableList<InputChoice?> = odrItem.customize
            .getOrNull()?.toMutableList() ?: mutableListOf()
        if (tabIdx < customize.size) {
            val choices: List<InputOption?> = chosen.choices
            var sumOfPrice = 0.0
            for (option in choices) {
                sumOfPrice += option?.price ?: 0.0
            }
            val newCost: Double = sumOfPrice * chosen.amount
            val newChoice = getNewChoice(chosen.amount, newCost)
            customize[tabIdx] = updateChosenOptions(newChoice, choices)
            odrItem = updateInputOrder(customize)
        }
    }

    private fun actSyncTabIndex(tabIndex: Int, complete: (Int, InputChoice) -> Unit) {
        val customize: List<InputChoice?> = odrItem.customize.getOrNull() ?: listOf()
        val total: Int = customize.size
        if (tabIndex in 0 until total) {
            val chosen = customize[tabIndex]
            if (chosen != null) {
                CoroutineScope(Dispatchers.Main).launch {
                    complete(tabIndex, chosen)
                }
            }
        }
    }

    private fun actStoreTotalRemain(remain: Int) {
        launch {
            val total: Int = odrItem.quantity - remain
            val chosen = UpdateTotalChosen(
                spotId = odrItem.item.spotId, total = total
            )
            archmage.beChant(MassTeleport(cargo = chosen))
        }
    }

    private fun actCustomize(complete: (() -> Unit)?) {
        launch {
            val customize: MutableList<InputChoice?> = odrItem.customize
                .getOrNull()?.toMutableList() ?: mutableListOf()
            customize.removeIf { (it?.choices?.size ?: 0) == 0 }
            customize.removeIf { (it?.amount ?: 0) == 0 }
            odrItem = updateInputOrder(customize)
            val custom = CalculateCustom(itemIndex, odrItem)
            archmage.beChant(MassTeleport(cargo = custom))
            withContext(Dispatchers.Main) {
                complete?.let { it() }
            }
        }
    }


    /** ------------------------------------------------------------------------------------------------ **/

    override fun beCollectParcels(complete: (InputOrderItem, Int) -> Unit) {
        tell { actCollectParcels(complete) }
    }

    override fun beAddChoice(choice: InputChoice, complete: (() -> Unit)?) {
        tell { actAddChoice(choice, complete) }
    }

    override fun beRemoveTab(
        tabIdx: Int, chosen: InputChoice,
        complete: ((idx: Int, amount: Int) -> Unit)?
    ) {
        tell { actRemoveTab(tabIdx, chosen, complete) }
    }

    override fun bePickOption(
        option: InputOption, choice: InputChoice,
        tabIdx: Int, complete: (InputChoice) -> Unit
    ) {
        tell { actPickOption(option, choice, tabIdx, complete) }
    }

    override fun beUnPickOption(
        option: InputOption, idx: Int, choice: InputChoice,
        tabIdx: Int, complete: (InputChoice, Int) -> Unit
    ) {
        tell { actUnPickOption(option, idx, choice, tabIdx, complete) }
    }

    override fun beOrderAmount(complete: (Int) -> Unit) {
        tell { actOrderAmount(complete) }
    }

    override fun beIncrease(
        value: Int, chosen: InputChoice,
        idx: Int, complete: (Int, InputChoice) -> Unit
    ) {
        tell { actIncrease(value, chosen, idx, complete) }
    }

    override fun beCalculateChosen(chosen: InputChoice, tabIdx: Int) {
        tell { actCalculateChosen(chosen, tabIdx) }
    }

    override fun beSyncTabIndex(tabIndex: Int, complete: (Int, InputChoice) -> Unit) {
        tell { actSyncTabIndex(tabIndex, complete) }
    }

    override fun beStoreTotalRemain(remain: Int) {
        tell { actStoreTotalRemain(remain) }
    }

    override fun beCustomize(complete: (() -> Unit)?) {
        tell { actCustomize(complete) }
    }
}