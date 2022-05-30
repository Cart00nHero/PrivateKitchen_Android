package com.cartoonhero.privatekitchen_android.actors.generator

import com.cartoonhero.theatre.Actor
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class Generator: Actor() {
    suspend fun beSpotId(): Long {
        val actorJob = CompletableDeferred<Long>()
        tell {
            val snowId = SnowFlake(1)
            actorJob.complete(snowId.nextId())
        }
        return actorJob.await()
    }
}