package com.cartoonhero.privatekitchen_android.props.entities

import graphqlApollo.operation.type.DiningWay

data class Workstation(
    val uniqueId: String?,
    val chefId: String?,
    val kitchenId: String?,
    var info: StationInfo?,
    var kitchen: StKitchen?
//    var menu: Template?
)
data class StationInfo(
    var chef: String? = ""
)
data class StKitchen(
    var info: KitchenInfo? = KitchenInfo(),
    var address: GQAddress? = GQAddress()
)
data class Orderer(
    val name: String?,
    val phone: String?
)
data class OrderInfo(
    var cost: Double? = 0.0,
    var people: Int? = 0
)
data class Storehouse(
    val ownerId: String?,
    var items: List<MenuItem>? = listOf(),
    var options: List<GQOption>? = listOf(),
    var diningWays: List<DiningWay>? = listOf()
)
