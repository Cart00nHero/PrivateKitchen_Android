package com.cartoonhero.theatre

import com.cartoonhero.theatre.Actor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
abstract class Scenario: Actor(), CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job
}