package com.cartoonhero.privatekitchen_android.patterns.obTransfer

import com.cartoonhero.privatekitchen_android.actors.Transformer
import com.cartoonhero.privatekitchen_android.actors.generator.Generator
import com.cartoonhero.privatekitchen_android.actors.objBox.ObDb
import com.cartoonhero.privatekitchen_android.props.entities.GQCategory
import com.cartoonhero.privatekitchen_android.props.entities.GQPage
import com.cartoonhero.privatekitchen_android.props.entities.MenuItem
import com.cartoonhero.privatekitchen_android.props.entities.Template
import com.cartoonhero.privatekitchen_android.props.obEntities.*
import com.cartoonhero.theatre.Pattern
import com.cartoonhero.theatre.Scenario
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class TemplateTransfer(attach: Scenario) : Pattern(attach) {
    private lateinit var fromObj: Template
    private lateinit var toObj: ObTemplate

    private fun actSet(from: Template) {
        fromObj = from
        toObj = ObTemplate(id = 0, ownerId = from.ownerId)
    }

    private fun actTransfer(myJob: CompletableDeferred<ObTemplate>) {
        launch {
            if (cleanDB()) {
                val tempBox = ObDb().beTakeBox(ObTemplate::class.java)
                tempBox.put(toObj)
                toObj = tempBox.all.first()
                startTransfer()
                myJob.complete(tempBox.get(toObj.id))
            }
        }
    }

    /** ------------------------------------------------------------------------------------------------ **/

    private suspend fun cleanDB(): Boolean {
        val tempBox = ObDb().beTakeBox(ObTemplate::class.java)
        val pageBox = ObDb().beTakeBox(ObPage::class.java)
        val ctgBox = ObDb().beTakeBox(ObCategory::class.java)
        ctgBox.removeAll()
        pageBox.removeAll()
        tempBox.removeAll()
        return true
    }

    private suspend fun startTransfer() {
        val pages: List<ObPage> = transfer(fromObj.pages ?: listOf())
        val tempBox = ObDb().beTakeBox(ObTemplate::class.java)
        toObj.pages.addAll(pages)
        toObj.pages.applyChangesToDb()
        tempBox.put(toObj)
    }

    private suspend fun transfer(pages: List<GQPage>): List<ObPage> {
        val obPages: MutableList<ObPage> = mutableListOf()
        for (i in pages.indices) {
            val obPage = ObPage(id = 0, pagination = i)
            obPages.add(obPage)
        }
        val pageBox = ObDb().beTakeBox(ObPage::class.java)
        pageBox.put(obPages)
        val newPages: List<ObPage> = pageBox.all
        obPages.clear()
        for (i in newPages.indices) {
            val page: GQPage = pages[i]
            val categories = transfer(
                page.categories ?: listOf(),
                newPages[i]
            )
            newPages[i].categories.addAll(categories)
            newPages[i].categories.applyChangesToDb()
        }
        pageBox.put(newPages)
        return pageBox.all
    }

    private suspend fun transfer(categories: List<GQCategory>, page: ObPage): List<ObCategory> {
        val obCategories: MutableList<ObCategory> = mutableListOf()
        for (i in categories.indices) {
            val gqCtg: GQCategory = categories[i]
            val tempId = Generator().beSpotId()
            val spoId: Long = gqCtg.spotId?.toLong() ?: tempId
            val obCategory = ObCategory(id = 0, spotId = spoId, sequence = i)
            obCategory.toPage.target = page
            if (gqCtg.titleText != null) {
                obCategory.titleText = Transformer().beToJson(gqCtg.titleText)
            }
            obCategories.add(obCategory)
        }
        val cBox = ObDb().beTakeBox(ObCategory::class.java)
        cBox.put(obCategories)
        val newCategories: List<ObCategory> = cBox.all
        for (i in newCategories.indices) {
            val gqCtg: GQCategory = categories[i]
            val items = transfer(
                gqCtg.items ?: listOf(), newCategories[i]
            )
            newCategories[i].items.addAll(items)
            newCategories[i].items.applyChangesToDb()
        }
        return newCategories
    }

    private suspend fun transfer(items: List<MenuItem>, category: ObCategory): List<ObMenuItem> {
        val obItems: MutableList<ObMenuItem> = mutableListOf()
        val itemBox = ObDb().beTakeBox(ObMenuItem::class.java)
        for (item in items) {
            val tempId = Generator().beSpotId()
            val spotId: Long = item.spotId?.toLong() ?: tempId
            val query = itemBox.query(
                ObMenuItem_.spotId.equal(spotId)
            ).build()
            val found = query.findUnique()
            if (found != null) {
                found.toCategory.target = category
                obItems.add(found)
            }
        }
        return obItems
    }

    /** ------------------------------------------------------------------------------------------------ **/

    fun beSet(from: Template) {
        tell { actSet(from) }
    }

    suspend fun beTransfer(): ObTemplate? {
        val actorJob = CompletableDeferred<ObTemplate>()
        tell { actTransfer(actorJob) }
        return actorJob.await()
    }
}