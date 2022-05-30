package com.cartoonhero.privatekitchen_android.actors.objBox

import android.content.Context
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
    fun beDebut(context: Context) {
        tell {
            ObjBox.createStore(context)
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
        fun createStore(context: Context) {
            store = MyObjectBox.builder()
                .androidContext(context.applicationContext).build()
        }
    }
}