package com.cartoonhero.privatekitchen_android.stage.scenarios.menu

import com.apollographql.apollo3.api.Optional
import com.cartoonhero.privatekitchen_android.actors.Transformer
import com.cartoonhero.privatekitchen_android.actors.apolloApi.*
import com.cartoonhero.privatekitchen_android.actors.archmage.*
import com.cartoonhero.privatekitchen_android.actors.objBox.ObDb
import com.cartoonhero.privatekitchen_android.patterns.Transcribe
import com.cartoonhero.privatekitchen_android.patterns.obTransfer.TemplateTransfer
import com.cartoonhero.privatekitchen_android.props.EditMenuComplete
import com.cartoonhero.privatekitchen_android.props.entities.*
import com.cartoonhero.privatekitchen_android.props.entities.LocalizedText
import com.cartoonhero.privatekitchen_android.props.entities.MenuItem
import com.cartoonhero.privatekitchen_android.props.obEntities.*
import com.cartoonhero.privatekitchen_android.stage.scene.menu.EditMenuDirector
import com.cartoonhero.theatre.Courier
import com.cartoonhero.theatre.Scenario
import graphqlApollo.client.type.*
import graphqlApollo.operation.FindWorkstationQuery
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
    private fun actSaveCategory(title: String, items: List<ObMenuItem>) {
        if (!pageEditing) return
        launch {
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
                archmage.beChant(LiveScene(EditMenuComplete()))
            }
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

    private fun actPublish(complete: (Boolean) -> Unit) {
        Helios(this@EditMenuScenario)
            .beFindWorkstation(stationId) { status, respObj ->
                when(status) {
                    ApiStatus.SUCCESS -> {
                        val addressJob: Deferred<InputAddress> = async {
                            var inputAddress = InputAddress(latitude = "0.0", longitude = "0.0")
                            if (respObj?.kitchen?.address != null) {
                                val address = Transformer()
                                    .beTransfer<FindWorkstationQuery.Address, GQAddress>(
                                        respObj.kitchen.address
                                    )
                                if (address != null) {
                                    inputAddress = Transcribe(this@EditMenuScenario).beGQAddressToInput(address)
                                }
                            }
                            inputAddress
                        }
                        val menuJob: Deferred<InputMenu> = async {
                            var menuInput = InputMenu(ownerId = respObj?.kitchenId ?: "")
                            if (respObj?.menu != null) {
                                val gqMenu = Transformer()
                                    .beTransfer<FindWorkstationQuery.Menu, GQMenu>(respObj.menu)
                                if (gqMenu != null) {
                                    val pageInputs = getPublishPages(gqMenu.pages ?: listOf())
                                    menuInput = InputMenu(
                                        ownerId = respObj.kitchenId ?: "",
                                        pages = Optional.presentIfNotNull(pageInputs)
                                    )
                                }
                            }
                            menuInput
                        }
                        launch {
                            val infoInput = InputKitchenInfo(
                                name = respObj?.kitchen?.info?.name ?: "",
                                phone = respObj?.kitchen?.info?.phone ?: ""
                            )
                            val kitchenIn = InputKitchen(
                                address = addressJob.await(),
                                info = Optional.presentIfNotNull(infoInput),
                                menu = menuJob.await(), uniqueId = respObj?.kitchenId ?: ""
                            )
                            publish(kitchenIn, respObj?.chefId ?: "", complete)
                        }
                    }
                    ApiStatus.FAILED -> CoroutineScope(Dispatchers.Main).launch {
                        complete(false)
                    }
                }
            }
    }
    private fun actLowerCurtain() {
        archmage.beShutOff()
    }

    /** ------------------------------------------------------------------------------------------------ **/

    private suspend fun searchMenu() {
        val tempBox = ObDb().beTakeBox(ObTemplate::class.java)
        val query = tempBox.query(
            ObTemplate_.ownerId.equal(stationId)
        ).build()
        val found = query.findUnique()
        if (found == null) {
            /* Test code
            val newMenu = ObTemplate(id = 0, ownerId = stationId)
            tempBox.put(newMenu)
            queryObTemplate()
            */
            syncMenu()
        } else {
            tempMenu = found
            buildPageVMs()
        }
    }

    private fun syncMenu() {
        Helios(this).beFindWorkstation(stationId) { status, respData ->
            when(status) {
                ApiStatus.SUCCESS -> launch {
                    if (respData?.menu != null) {
                        val newMenu = Transformer()
                            .beTransfer<FindWorkstationQuery.Menu, Template>(respData.menu)
                        if (newMenu != null) {
                            val transfer = TemplateTransfer(this@EditMenuScenario)
                            transfer.beSet(newMenu)
                            tempMenu = transfer.beTransfer()
                            buildPageVMs()
                        }
                    }
                }
                ApiStatus.FAILED -> print("Not found")
            }
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
            val itemInputs = transformItems(obCtg.items)
            val ctgInput = InputStoreCategory(
                sequence = i, spotId = obCtg.spotId.toString(),
                titleText = Optional.presentIfNotNull(titleInput),
                items = Optional.presentIfNotNull(itemInputs)
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

    private suspend fun getPublishPages(pages: List<GQPage>): List<InputPage> {
        val pageInputs = mutableListOf<InputPage>()
        for (page in pages) {
            val categoryInputs = getPublishCategories(
                page.categories ?: listOf()
            )
            val inputPage = InputPage(
                categories = Optional.presentIfNotNull(categoryInputs),
                pagination = page.pagination ?: 0,
            )
            pageInputs.add(inputPage)
        }
        return pageInputs
    }
    private suspend fun getPublishCategories(categories: List<GQCategory>): List<InputCategory> {
        val categoryInputs = mutableListOf<InputCategory>()
        for (category in categories) {
            val inputTitle = category.titleText?.let {
                Transcribe(this).beGetInputText(it)
            }
            val inputItems = getPublishItems(category.items ?: listOf())
            val inputCategory = InputCategory(
                items = inputItems,
                sequence = category.sequence ?: 0,
                titleText = inputTitle ?: InputText(local = "")
            )
            categoryInputs.add(inputCategory)
        }
        return  categoryInputs
    }
    private suspend fun getPublishItems(items: List<MenuItem>): List<InputItem> {
        val itemInputs = mutableListOf<InputItem>()
        for (item in items) {
            val inputIntro = item.introText?.let {
                Transcribe(this).beGetInputText(it)
            }
            val inputName = item.nameText?.let {
                Transcribe(this).beGetInputText(it)
            }
            val customs = getPublishCustomizations(
                item.customizations ?: listOf()
            )
            val inputItem = InputItem(
                customizations = Optional.presentIfNotNull(customs),
                introText = Optional.presentIfNotNull(inputIntro),
                nameText = inputName ?: InputText(local = ""),
                photo = Optional.presentIfNotNull(item.photo),
                price = item.price ?: 0.0, quota = item.quota ?: 0,
                sequence = item.sequence ?: 0, spotId = Optional.presentIfNotNull(item.spotId)
            )
            itemInputs.add(inputItem)
        }
        return itemInputs
    }
    private suspend fun getPublishCustomizations(customizations: List<GQOption>): List<InputItemOption> {
        val customInputs = mutableListOf<InputItemOption>()
        for (i in customizations.indices) {
            val customization = customizations[i]
            val inputTitle = customization.titleText?.let {
                Transcribe(this).beGetInputText(it)
            }
            val inputCustom = InputItemOption(
                price = Optional.presentIfNotNull(customization.price),
                sequence = Optional.presentIfNotNull(i),
                spotId = Optional.presentIfNotNull(customization.spotId),
                titleText = Optional.presentIfNotNull(inputTitle)
            )
            customInputs.add(inputCustom)
        }
        return customInputs
    }

    private fun publish(
        kitchen: InputKitchen, chefId: String,
        complete: (Boolean) -> Unit
    ) {
        val mainScope = CoroutineScope(Dispatchers.Main)
        Icarus(this).bePublishKitchen(chefId, kitchen) { status, _ ->
            when (status) {
                ApiStatus.SUCCESS -> mainScope.launch {
                    complete(true)
                }
                ApiStatus.FAILED -> mainScope.launch {
                    complete(false)
                }
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

    override fun beSaveCategory(title: String, items: List<ObMenuItem>) {
        tell { actSaveCategory(title, items) }
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

    override fun bePublish(complete: (Boolean) -> Unit) {
        tell { actPublish(complete) }
    }

    override fun beLowerCurtain() {
        tell { actLowerCurtain() }
    }
}