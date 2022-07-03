package com.cartoonhero.privatekitchen_android.actors.objBox

import com.cartoonhero.privatekitchen_android.props.mainContext
import com.cartoonhero.privatekitchen_android.props.obEntities.MyObjectBox
import com.cartoonhero.theatre.Actor
import io.objectbox.Box
import io.objectbox.BoxStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class ObDb : Actor() {

    private fun actDebut() {
        ObjBox.createStore()
    }

    private fun <T> actTakeBox(boxType: Class<T>): Box<T> {
        return ObjBox.store.boxFor(boxType)
    }

    /** ----------------------------------------------------------------------------------------------------- **/

    fun beDebut() {
        tell {
            actDebut()
        }
    }

    suspend fun <T> beTakeBox(boxType: Class<T>): Box<T> {
        val actorJob = CompletableDeferred<Box<T>>()
        tell {
            actorJob.complete(actTakeBox(boxType))
        }
        return actorJob.await()
    }

    private object ObjBox {
        lateinit var store: BoxStore
            private set

        fun createStore() {
            store = MyObjectBox.builder()
                .androidContext(mainContext.applicationContext).build()
        }
    }
}