package com.cartoonhero.privatekitchen_android.patterns

import com.apollographql.apollo3.api.Optional
import com.cartoonhero.privatekitchen_android.actors.Transformer
import com.cartoonhero.privatekitchen_android.props.entities.*
import com.cartoonhero.privatekitchen_android.props.obEntities.ObAddress
import com.cartoonhero.theatre.Pattern
import com.cartoonhero.theatre.Scenario
import graphqlApollo.operation.type.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class Transcribe(attach: Scenario) : Pattern(attach) {

    private fun actLocalizedTextTo(
        text: LocalizedText,
        aJob: CompletableDeferred<InputOpText>
    ) {
        launch {
            val inputText = InputOpText(
                local = Optional.presentIfNotNull(text.local)
            )
            aJob.complete(inputText)
        }
    }
    private fun actGQAddressTo(
        address: GQAddress, aJob: CompletableDeferred<ObAddress>
    ) {
        launch {
            val addressMap: Map<String,Any> = Transformer().beEntityToMap(
                address
            ) ?: mapOf()
            val modifyMap: MutableMap<String,Any> = addressMap.toMutableMap()
            modifyMap["id"] = 0
            val obAddress: ObAddress = Transformer().beMapToEntity<Any,ObAddress>(
                modifyMap
            ) ?: ObAddress(id = 0)
            aJob.complete(obAddress)
        }
    }
    private fun actObAddressTo(address: ObAddress): InputOpAddress {
        return InputOpAddress(
            administrativeArea = Optional.presentIfNotNull(address.administrativeArea),
            completion = Optional.presentIfNotNull(address.completion),
            floor = Optional.presentIfNotNull(address.floor),
            isoNationCode = Optional.presentIfNotNull(address.isoNationCode),
            latitude = address.latitude,
            locality = Optional.presentIfNotNull(address.locality),
            longitude = address.longitude,
            nation = Optional.presentIfNotNull(address.nation),
            plusCode = Optional.presentIfNotNull(address.plusCode),
            postalCode = Optional.presentIfNotNull(address.postalCode),
            subAdministrativeArea = Optional.presentIfNotNull(address.subAdministrativeArea),
            subLocality = Optional.presentIfNotNull(address.subLocality),
            subThoroughfare = Optional.presentIfNotNull(address.subThoroughfare),
            thoroughfare = Optional.presentIfNotNull(address.thoroughfare),
        )
    }

    suspend fun beLocalizedTextTo(text: LocalizedText): InputOpText {
        val actorJob = CompletableDeferred<InputOpText>()
        tell {
            actLocalizedTextTo(text,actorJob)
        }
        return actorJob.await()
    }
    suspend fun beObAddressTo(address: ObAddress): InputOpAddress {
        val actorJob = CompletableDeferred<InputOpAddress>()
        tell {
            actorJob.complete(actObAddressTo(address))
        }
        return actorJob.await()
    }
    suspend fun beGQAddressTo(address: GQAddress): ObAddress {
        val actorJob = CompletableDeferred<ObAddress>()
        tell {
            actGQAddressTo(address, actorJob)
        }
        return actorJob.await()
    }
}