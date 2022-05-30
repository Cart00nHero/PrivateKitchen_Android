package com.cartoonhero.theatre

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.EmptyCoroutineContext

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
abstract class Actor {
    private interface Narrative
    private val scenarist = Scenarist()
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private data class Plot(
        val act: () -> Unit
    ): Narrative

    init {
        startScope()
    }

    private fun startScope() = scope.launch {
        val actor = actor<Narrative>(scope.coroutineContext) {
            consumeEach { trope ->
                when(trope) {
                    is Plot -> trope.act()
                }
            }
        }
        scenarist.writings.collect(actor::send)
    }
    private fun portray(narrative: Narrative) = scenarist.portray(narrative)

    fun start() {
        if (!scope.isActive) startScope()
    }
    fun tell(_plot: () -> Unit) {
        portray(Plot(_plot))
    }
    fun cancel() {
        if (scope.isActive) scope.cancel()
    }

    @ExperimentalCoroutinesApi
    private inner class Scenarist {
        private val scope: CoroutineScope =
            CoroutineScope(EmptyCoroutineContext + SupervisorJob())
        private val channel: Channel<Narrative> = Channel(100)
//    private val channel: BroadcastChannel<Message> = BroadcastChannel(100)

        fun portray(narrative: Narrative) {
            scope.launch {
                channel.send(narrative)
            }
        }

        val writings: Flow<Narrative>
            get() = flow { emitAll(channel.receiveAsFlow()) }
//        get() = flow { emitAll(channel.openSubscription()) }
    }
}