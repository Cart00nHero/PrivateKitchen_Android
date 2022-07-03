package com.cartoonhero.privatekitchen_android.stage.scenarios.menu

import com.apollographql.apollo3.api.Optional
import com.cartoonhero.privatekitchen_android.actors.Transformer
import com.cartoonhero.privatekitchen_android.actors.apolloApi.*
import com.cartoonhero.privatekitchen_android.actors.archmage.*
import com.cartoonhero.privatekitchen_android.actors.objBox.ObDb
import com.cartoonhero.privatekitchen_android.patterns.Transcribe
import com.cartoonhero.privatekitchen_android.props.entities.*
import com.cartoonhero.privatekitchen_android.props.entities.Workstation
import com.cartoonhero.privatekitchen_android.props.obEntities.*
import com.cartoonhero.privatekitchen_android.stage.scene.menu.EditMenuDirector
import com.cartoonhero.theatre.Courier
import com.cartoonhero.theatre.Scenario
import graphqlApollo.client.type.InputKitchen
import graphqlApollo.client.type.InputMenu
import graphqlApollo.operation.SearchMatchedWorkstationsQuery
import graphqlApollo.operation.type.*
import io.objectbox.kotlin.and
import kotlinx.coroutines.*

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class EditMenuScenario : Scenario(), EditMenuDirector {
    private var stationId: String = ""
    private var tempMenu: ObTemplate? = null
    private var pageEditing: Boolean = false
    private val unPicked: MutableList<ObMenuItem> = mutableListOf()
    private val archmage: Archmage by lazy {
        Archmage(this)
    }

    private fun actShowTime(teleport: Teleporter) {
        archmage.beSetWaypoint(teleport)
    }

    private fun actCollectParcels() {
        launch {
            val pSet = Courier(this@EditMenuScenario).beClaim()
            for (parcel in pSet) {
                when (val content = parcel.content) {
                    is ObWorkstation -> {
                        stationId = content.uniqueId ?: ""
                        if (content.menu.target != null) {
                            tempMenu = content.menu.target
                            buildPageVMs()
                        } else {
                            searchMenu()
                        }
                    }
                }
            }
        }
    }

    private fun actAddPage(pagination: Int, complete: (() -> Unit)?) {
        if (tempMenu == null) return
        launch {
            val newPage = ObPage(
                id = 0, pagination = pagination
            )
            val tempBox = ObDb().beTakeBox(ObTemplate::class.java)
            tempMenu!!.pages.add(newPage)
            tempMenu!!.pages.applyChangesToDb()
            tempBox.put(tempMenu!!)
            queryObTemplate()
            withContext(Dispatchers.Main) {
                complete?.invoke()
            }
        }
    }

    private fun actAddCategory(page: ObPage, complete: (() -> Unit)?) {
        if (tempMenu == null) return
        launch {
            val seqNo: Int = page.categories.size
            val nowPage: ObPage = page
            val newCtg = ObCategory(
                id = 0, sequence = seqNo
            )
            val pageBox = ObDb().beTakeBox(ObPage::class.java)
            newCtg.toPage.setAndPutTarget(nowPage)
            nowPage.categories.add(newCtg)
            nowPage.categories.applyChangesToDb()
            pageBox.put(nowPage)
            queryObTemplate()
            withContext(Dispatchers.Main) {
                complete?.let { it() }
            }
        }
    }

    private fun actRemove(page: ObPage) {
        if (tempMenu == null) return
        val cleanJob: Deferred<Int> = async {
            val pagination: Int = page.pagination ?: 0
            val pBox = ObDb().beTakeBox(ObPage::class.java)
            val cBox = ObDb().beTakeBox(ObCategory::class.java)
            val query = cBox.query(
                ObCategory_.toPageId.equal(page.id)
            ).build()
            cBox.remove(query.find())
            pBox.remove(page.id)
            pagination
        }
        launch {
            val pBox = ObDb().beTakeBox(ObPage::class.java)
            val pagination = cleanJob.await()
            val query = pBox.query(
                ObPage_.pagination.greater(pagination)
            ).build()
            val sortPages: List<ObPage> = query.find()
            for (i in sortPages.indices) {
                val bookmark: Int = sortPages[i].pagination ?: i
                sortPages[i].pagination = bookmark - 1
            }
            pBox.put(sortPages)
            queryObTemplate()
            queryUnpicked()
        }
    }

    private fun actRemove(page: ObPage, atCIdx: Int) {
        val pgCategories: List<ObCategory> = page.categories
        if (atCIdx > pgCategories.size) return
        val removed = CompletableDeferred<Boolean>()
        launch {
            val category = pgCategories[atCIdx]
            val cBox = ObDb().beTakeBox(ObCategory::class.java)
            cBox.remove(category.id)
            removed.complete(true)
            val query = cBox.query(
                ObCategory_.toPageId.equal(page.id)
                        and
                        ObCategory_.sequence.greater(atCIdx)
            ).build()
            val sortData: List<ObCategory> = query.find()
            for (i in sortData.indices) {
                val idx: Int = sortData[i].sequence ?: i
                sortData[i].sequence = idx - 1
            }
            cBox.put(sortData)
            queryObTemplate()
        }
        launch {
            if (removed.await()) {
                queryUnpicked()
            }
        }
    }

    private fun actSetEdit(category: ObCategory, page: ObPage) {
        if (tempMenu == null) return
        launch {
            pageEditing = true
            val newMenu: ObTemplate = tempMenu!!
            page.categories.clear()
            page.categories.add(category)
            newMenu.pages.clear()
            newMenu.pages.add(page)
            tempMenu = newMenu
            buildPageVMs()
            queryUnpicked()
            val picked = category.items
            archmage.beChant(LiveScene(PickedItems(picked)))
        }
    }

    private fun actUpdate(title: String, category: ObCategory) {
        if (!pageEditing) return
        launch {
            val temp: ObCategory = category
            temp.titleText = Transformer().beToJson(LocalizedText(local = title))
            tempMenu!!.pages.first().categories[0] = temp
            buildPageVMs()
        }
    }

    private fun actPickUnPick(item: ObMenuItem) {
        if (!pageEditing) return
        val editing: ObCategory = tempMenu!!.pages.first().categories.first()
        val editItems: MutableList<ObMenuItem> = editing.items
        if (item.toCategory.isNull) {
            item.toCategory.setAndPutTarget(editing)
            unPicked.removeIf { it.spotId == item.spotId }
        } else {
            item.toCategory.target = null
            unPicked.add(item.sequence, item)
            editItems.removeIf { it.spotId == item.spotId }
        }
        editItems.sortBy { it.sequence }
        launch {
            archmage.beChant(LiveScene(UnPickedItems(unPicked)))
            archmage.beChant(LiveScene(PickedItems(editItems)))
        }
        editing.items.clear()
        editing.items.addAll(editItems)
        tempMenu!!.pages.first().categories[0] = editing
        buildPageVMs()
    }

    private fun actUpload(complete: (Boolean) -> Unit) {
        if (tempMenu == null) return
        launch {
            val pageInputs: MutableList<InputStorePage> = mutableListOf()
            val obPages: List<ObPage> = tempMenu!!.pages
            val sortPages: MutableList<ObPage> = mutableListOf()
            for (i in obPages.indices) {
                val obPage = obPages[i]
                obPage.pagination = i
                sortPages.add(obPage)
                val categories: List<ObCategory> = obPage.categories
                val inputCategories = transformCategories(categories)
                val pageInput = InputStorePage(
                    pagination = i,
                    categories = Optional.presentIfNotNull(inputCategories)
                )
                pageInputs.add(pageInput)
            }
            val tempInput = InputTemplet(pages = Optional.presentIfNotNull(pageInputs))
            val modify = ModifyWorkstation(menu = Optional.presentIfNotNull(tempInput))
            Helios(this@EditMenuScenario).beUpdateWorkstation(
                stationId, modify
            ) { status, _ ->
                when (status) {
                    ApiStatus.SUCCESS -> CoroutineScope(Dispatchers.Main).launch {
                        complete(true)
                    }
                    ApiStatus.FAILED -> CoroutineScope(Dispatchers.Main).launch {
                        complete(false)
                    }
                }
            }
        }
    }

    fun actPublish() {
        val query = QueryWorkstation(uniqueId = Optional.presentIfNotNull(stationId))
        Helios(this).beSearchMatchedWorkstations(query) { status, stations ->
            when (status) {
                ApiStatus.SUCCESS -> {
                    if (stations.isNullOrEmpty()) {
                        launch {
                            val wkStation = stations?.first()
                            val workstation = Transformer()
                                .beTransfer<SearchMatchedWorkstationsQuery
                                .SearchMatchedWorkstation?, Workstation>(wkStation)
                            var inputAddress: InputOpAddress? = null
                            var inputMenu: InputMenu? = null
                            if (workstation?.kitchen?.address != null) {
                                inputAddress = Transcribe(this@EditMenuScenario)
                                    .beGQAddressToInput(workstation.kitchen?.address!!)
                            }
                            if (workstation!!.menu != null) {
                            }

                        }
                    }
                }
                ApiStatus.FAILED -> TODO()
            }
        }
    }

    /** ------------------------------------------------------------------------------------------------ **/

    private suspend fun searchMenu() {
        val tempBox = ObDb().beTakeBox(ObTemplate::class.java)
        val query = tempBox.query(
            ObTemplate_.ownerId.equal(stationId)
        ).build()
        val found = query.findUnique()
        if (found == null) {
            val newMenu = ObTemplate(id = 0, ownerId = stationId)
            tempBox.put(newMenu)
            queryObTemplate()
        } else {
            tempMenu = found
        }
    }

    private fun buildPageVMs() {
        if (tempMenu == null) return
        launch {
            val pageVMs: MutableList<MenuPageVM> = mutableListOf()
            val sortedPages = tempMenu!!.pages.sortedBy { it.pagination }
            for (obPage in sortedPages) {
                pageVMs.add(MenuPageVM(page = obPage))
            }
            if (pageEditing) {
                pageVMs.add(MenuPageVM(null))
            }
            archmage.beChant(LiveScene(prop = pageVMs))
        }
    }

    private suspend fun queryUnpicked() {
        val itemBox = ObDb().beTakeBox(ObMenuItem::class.java)
        val query = itemBox.query(
            ObMenuItem_.toCategoryId.equal(0)
        ).build()
        unPicked.clear()
        unPicked.addAll(query.find())
        archmage.beChant(LiveScene(UnPickedItems(unPicked)))
    }

    private suspend fun queryObTemplate() {
        val tempBox = ObDb().beTakeBox(ObTemplate::class.java)
        val query = tempBox.query(
            ObTemplate_.ownerId.equal(stationId)
        ).build()
        val newMenu = query.findUnique()
        if (newMenu != null) {
            tempMenu = newMenu
            buildPageVMs()
        }
    }

    private suspend fun transformCategories(categories: List<ObCategory>): List<InputStoreCategory> {
        val ctgInputs: MutableList<InputStoreCategory> = mutableListOf()
        for (i in categories.indices) {
            val obCtg = categories[i]
            val titleInput: InputOpText = Transcribe(this).beLocalizedTextTo(
                obCtg.beTitle()
            )
            val ctgInput = InputStoreCategory(
                sequence = i, spotId = obCtg.spotId.toString(),
                titleText = Optional.presentIfNotNull(titleInput)
            )
            ctgInputs.add(ctgInput)
        }
        return ctgInputs
    }

    private suspend fun transformItems(items: List<ObMenuItem>): List<InputMenuItem> {
        val itemInputs: MutableList<InputMenuItem> = mutableListOf()
        for (i in items.indices) {
            val obItem = items[i]
            val nameInput = Transcribe(this).beLocalizedTextTo(obItem.beName())
            val introInput = Transcribe(this).beLocalizedTextTo(obItem.beIntro())
            val itemInput = InputMenuItem(
                customizations = listOf(),
                photo = Optional.presentIfNotNull(obItem.photo),
                price = obItem.price, sequence = i,
                spotId = obItem.spotId.toString(),
                nameText = Optional.presentIfNotNull(nameInput),
                introText = Optional.presentIfNotNull(introInput)
            )
            itemInputs.add(itemInput)
        }
        return itemInputs
    }

    private suspend fun remove(category: ObCategory, page: ObPage) {
        val removeSeq: Int = category.sequence ?: 0
        val ctgBox = ObDb().beTakeBox(ObCategory::class.java)
        ctgBox.remove(category)
        val query = ctgBox.query(
            ObCategory_.toPageId.equal(page.id)
                    and
                    ObCategory_.sequence.greater(removeSeq)
        ).build()
        val sortData = query.find()
        for (i in 0 until sortData.size) {
            val idx: Int = sortData[i].sequence ?: 0
            sortData[i].sequence = idx - 1
        }
        ctgBox.put(sortData)
        queryUnpicked()
        queryObTemplate()
    }

    private suspend fun save(category: ObCategory) {
        if (!pageEditing) return
        val tmpCtg = tempMenu?.pages?.first()?.categories?.first()
        if (tmpCtg != null) {
            val ctgBox = ObDb().beTakeBox(ObCategory::class.java)
            ctgBox.put(tmpCtg)
            queryObTemplate()
            queryUnpicked()
        }
    }

    private suspend fun saveCategory(title: String, items: List<ObMenuItem>) {
        if (!pageEditing) return
        val ctgBox = ObDb().beTakeBox(ObCategory::class.java)
        val tmpCtg = tempMenu?.pages?.first()?.categories?.first()
        if (tmpCtg != null) {
            tmpCtg.titleText = Transformer().beToJson(
                LocalizedText(local = title)
            )
            tmpCtg.items.clear()
            tmpCtg.items.addAll(items)
            tmpCtg.items.applyChangesToDb()
            ctgBox.put(tmpCtg)
            pageEditing = false
            queryObTemplate()
            queryUnpicked()
        }
    }

    private fun publish(kitchen: InputKitchen, chefId: String) {
        Icarus(this).bePublishKitchen(chefId, kitchen) { status, respObj ->
            when (status) {
                ApiStatus.SUCCESS -> {
                    if (respObj != null) {
                        print("OK")
                    }
                }
                ApiStatus.FAILED -> print("Publish error")
            }
        }
    }

    /** ------------------------------------------------------------------------------------------------- **/

    override fun beShowTime(teleport: Teleporter) {
        tell { actShowTime(teleport) }
    }

    override fun beCollectParcels() {
        tell { actCollectParcels() }
    }

    override fun beAddPage(pagination: Int, complete: (() -> Unit)?) {
        tell { actAddPage(pagination, complete) }
    }

    override fun beAddCategory(page: ObPage, complete: (() -> Unit)?) {
        tell { actAddCategory(page, complete) }
    }

    override fun beRemove(page: ObPage) {
        tell { actRemove(page) }
    }

    override fun beRemove(page: ObPage, atCIdx: Int) {
        tell { actRemove(page, atCIdx) }
    }

    override fun beSetEdit(category: ObCategory, page: ObPage) {
        tell { actSetEdit(category, page) }
    }

    override fun beUpdate(title: String, category: ObCategory) {
        tell { actUpdate(title, category) }
    }

    override fun bePickUnPick(item: ObMenuItem) {
        tell { actPickUnPick(item) }
    }

    override fun beUpload(complete: (Boolean) -> Unit) {
        tell { actUpload(complete) }
    }

    override fun bePublish() {
        TODO("Not yet implemented")
    }

    override fun beLowerCurtain() {
        TODO("Not yet implemented")
    }
}