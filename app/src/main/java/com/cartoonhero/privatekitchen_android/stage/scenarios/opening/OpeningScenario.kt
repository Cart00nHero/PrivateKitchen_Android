package com.cartoonhero.privatekitchen_android.stage.scenarios.opening

import android.content.Context
import android.util.Log
import com.apollographql.apollo3.api.Optional
import com.cartoonhero.privatekitchen_android.actors.Transformer
import com.cartoonhero.privatekitchen_android.actors.apolloApi.*
import com.cartoonhero.privatekitchen_android.actors.archmage.*
import com.cartoonhero.privatekitchen_android.actors.objBox.ObDb
import com.cartoonhero.privatekitchen_android.patterns.obTransfer.WkStationTransfer
import com.cartoonhero.privatekitchen_android.props.entities.*
import com.cartoonhero.privatekitchen_android.props.entities.Workstation
import com.cartoonhero.privatekitchen_android.props.mainContext
import com.cartoonhero.privatekitchen_android.props.sharedStorage
import com.cartoonhero.privatekitchen_android.stage.scene.opening.OpeningDirector
import com.cartoonhero.theatre.Scenario
import graphqlApollo.operation.CreateWorkstationMutation
import graphqlApollo.operation.SearchMatchedWorkstationsQuery
import graphqlApollo.operation.type.*
import kotlinx.coroutines.*

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class OpeningScenario : Scenario(), OpeningDirector {
    private val archmage: Archmage by lazy {
        Archmage(this)
    }
    private var chefName: String = ""

    private fun actShowTime(teleporter: Teleporter) {
        archmage.beSetWaypoint(teleporter)
        launch {
            this@OpeningScenario.start()
            ObDb().beDebut()
        }
    }
    private fun actBuildSource() {
        val sharedPrefs = mainContext.getSharedPreferences(
            sharedStorage, Context.MODE_PRIVATE
        )
        val uid: String = sharedPrefs.getString("SignedUserId", "") ?: ""
        if (uid.isEmpty()) {
            val source: List<EnterListSource> = listOf(
                EnterListSource("Sign With Apple", SignType.Apple),
                EnterListSource("Facebook", SignType.Facebook)
            )
            archmage.beChant(LiveScene(source))
        } else {
            findMyWorkstation(uid)
        }
    }
    private fun actSignWithApple() {
    }
    private fun actSignWithGoogle() {
    }
    private fun actCreateWorkstation() {
        val sharedPrefs = mainContext.getSharedPreferences(
            sharedStorage, Context.MODE_PRIVATE
        )
        val uid: String = sharedPrefs.getString("SignedUserId", "") ?: ""
        val chefInfo = InputStInfo(chef = Optional.presentIfNotNull(chefName))
        val inputSt = InputWorkstation(
            chefId = uid,
            info = Optional.presentIfNotNull(chefInfo)
        )
        Helios(this).beCreateWorkstation(inputSt) { status, respObj ->
            when(status) {
                ApiStatus.SUCCESS -> {
                    if (respObj != null) {
                        launch {
                            val workstation = Transformer()
                                .beTransfer<CreateWorkstationMutation.CreateWorkstation,
                                        Workstation>(respObj)
                            if (workstation != null) {
                                tell { saveWorkstation(workstation) }
                            }
                        }
                    }
                }
                else -> {
                    Log.d("Create Station","Error")
                }
            }
        }
    }
    private fun actLowerCurtain() {
        archmage.beShutOff()
    }

/** -------------------------------------------------------------------------------------------------------------- **/

    private fun findMyWorkstation(chefId: String) {
        val query = QueryWorkstation(
            chefId = Optional.presentIfNotNull(chefId)
        )
        Helios(this).beSearchMatchedWorkstations(query) { status, respArr ->
            when (status) {
                ApiStatus.SUCCESS -> {
                    if (respArr != null && respArr.isNotEmpty()) {
                        val station = respArr.first()
                        launch {
                            val currentSt = Transformer().beTransfer<
                                    SearchMatchedWorkstationsQuery.SearchMatchedWorkstation?,
                                    Workstation>(station)
                            if (currentSt != null) {
                                tell {
                                    saveWorkstation(currentSt)
                                }
                            }
                        }
                    } else {
                        val source = listOf(
                            EnterListSource(
                                title = "Create My WorkStation",
                                type = SignType.CreateSt
                            )
                        )
                        archmage.beChant(LiveScene(source))
                    }
                }
                ApiStatus.FAILED -> {
                    print("beSearchMatchedWorkstations failed")
                }
            }
        }
    }
    private fun saveWorkstation(station: Workstation) {
        val transfer = WkStationTransfer(this)
        transfer.beSet(station)
        launch {
            val saved = transfer.beTransfer()
            if (saved != null) {
                archmage.beChant(LiveScene(prop = saved))
            }
        }
    }

/** -------------------------------------------------------------------------------------------------------------- **/

    override fun beShowTime(teleporter: Teleporter) {
        tell {
            actShowTime(teleporter)
        }
    }

    override fun beBuildSource() {
        tell {
            actBuildSource()
        }
    }

    override fun beSignWithApple() {
        tell {
            actSignWithApple()
        }
    }

    override fun beSignWithGoogle() {
        tell {
            actSignWithGoogle()
        }
    }

    override fun beCreateWorkstation() {
        tell {
            actCreateWorkstation()
        }
    }

    override fun beLowerCurtain() {
        tell {
            actLowerCurtain()
        }
    }

}