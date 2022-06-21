package com.cartoonhero.privatekitchen_android.props.entities

import com.apollographql.apollo3.api.Optional
import graphqlApollo.operation.type.ModifyOrder
import graphqlApollo.operation.type.QueryOrder

data class Kitchen(
    val uniqueId: String?,
    var info: KitchenInfo? = KitchenInfo(),
    var appraise: Int? = 0,
    var address: GQAddress? = GQAddress()
)
data class KitchenInfo(
    var name: String? = "",
    var phone: String? = ""
)
data class GQAddress(
    var administrativeArea: String? = "",
    var completion: String? = "",
    var isoNationCode: String? = "",
    var latitude: String? = "0.0",
    var longitude: String? = "0.0",
    var locality: String? = "",
    var nation: String? = "",
    var postalCode: String? = "",
    var subAdministrativeArea: String? = "",
    var subLocality: String? = "",
    var thoroughfare: String? = "",
    var subThoroughfare: String? = "",
    var floor: String? = "",
    var plusCode: String? = "",
)
data class GQPlace(
    var kitchen: Kitchen?,
    var administrativeArea: String? = "",
    var completion: String? = "",
    var isoNationCode: String? = "",
    var latitude: String? = "0.0",
    var longitude: String? = "0.0",
    var locality: String? = "",
    var nation: String? = "",
    var postalCode: String? = "",
    var subAdministrativeArea: String? = "",
    var subLocality: String? = "",
    var thoroughfare: String? = "",
    var subThoroughfare: String? = "",
    var floor: String? = "",
    var plusCode: String? = ""
)
data class LocalizedText(
    var local: String? = ""
)

data class OrderTimeFrame(
    var startDate: String?,
    var endDate: String?,
    var queries: QueryOrder?,
    var modifies: ModifyOrder?
)
