package com.cartoonhero.privatekitchen_android.actors.objBox

import android.content.Context
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
class ObDb: Actor() {
    fun beDebut() {
        tell {
            ObjBox.createStore()
        }
    }
    suspend fun <T> beTakeBox(boxType: Class<T>): Box<T> {
        val actorJob = CompletableDeferred<Box<T>>()
        tell {
            val theBox = ObjBox.store.boxFor(boxType)
            actorJob.complete(theBox)
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