package com.cartoonhero.privatekitchen_android.stage.scenarios.opening

import android.content.Context
import com.apollographql.apollo3.api.Optional
import com.cartoonhero.privatekitchen_android.actors.Transformer
import com.cartoonhero.privatekitchen_android.actors.apolloApi.ApiStatus
import com.cartoonhero.privatekitchen_android.actors.apolloApi.Helios
import com.cartoonhero.privatekitchen_android.actors.objBox.ObDb
import com.cartoonhero.privatekitchen_android.props.entities.EnterListSource
import com.cartoonhero.privatekitchen_android.props.entities.SignType
import com.cartoonhero.privatekitchen_android.props.entities.Workstation
import com.cartoonhero.privatekitchen_android.props.inlineTools.applyEdit
import com.cartoonhero.privatekitchen_android.props.mainContext
import com.cartoonhero.privatekitchen_android.props.sharedStorage
import com.cartoonhero.privatekitchen_android.stage.scene.opening.OpeningDirector
import com.cartoonhero.theatre.Scenario
import graphqlApollo.operation.SearchMatchedWorkstationsQuery
import graphqlApollo.operation.type.QueryWorkstation
import kotlinx.coroutines.*

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class OpeningScenario: Scenario(), OpeningDirector {
    private lateinit var sourceSubscriber:((List<EnterListSource>) -> Unit)

    private fun actShowTime() {
        launch {
            this@OpeningScenario.start()
            ObDb().beDebut(mainContext)
        }
    }
    private fun actBuildSource(complete: (List<EnterListSource>) -> Unit) {
        val sharedPrefs =  mainContext.getSharedPreferences(
            sharedStorage, Context.MODE_PRIVATE
        )
        val uid: String = sharedPrefs.getString("SignedUserId", "") ?: ""
        if (uid.isEmpty()) {
            val source: List<EnterListSource> = listOf(
                EnterListSource("Sign With Apple", SignType.Apple),
                EnterListSource("Facebook", SignType.Facebook)
            )
            CoroutineScope(Dispatchers.Main).launch {
                complete(source)
            }
        } else {

        }
    }
    private fun findMyWorkstation(chefId: String) {
        val query = QueryWorkstation(
            chefId = Optional.presentIfNotNull(chefId)
        )
        Helios(this).beSearchMatchedWorkstations(query) { status, respArr ->
            when(status) {
                ApiStatus.SUCCESS ->{
                    if (respArr != null && respArr.isNotEmpty()) {
                        val foundSt = respArr.first()
                    }
                }
                ApiStatus.FAILED -> {}
            }
        }
//        Helios(this).beSearchMatchedWorkstations(query) { status, respArr ->
//            when(status) {
//                ApiStatus.SUCCESS ->{
//                    if (respArr != null, && respArr)
//                }
//                ApiStatus.FAILED -> {}
//            }
//        }
//        Helios(this).beSearchMatchedWorkstations(query) {
//
//        }
    }


    override fun beShowTime() {
        tell {
            actShowTime()
        }
    }

    override fun beSubscribeListSource(subscriber: (List<EnterListSource>) -> Unit) {
        tell {
            sourceSubscriber = subscriber
        }
    }

    override fun beBuildSource(complete: (List<EnterListSource>) -> Unit) {
        tell {
            actBuildSource(complete)
        }
    }

    override fun beSignWithApple() {
        TODO("Not yet implemented")
    }

    override fun beSignWithFaceBook() {
        TODO("Not yet implemented")
    }

    override fun beCreateWorkstation() {
        TODO("Not yet implemented")
    }

    override fun beLowerCurtain() {
        TODO("Not yet implemented")
    }

}