package com.cartoonhero.privatekitchen_android.props.entities

data class StationInfo(
    var chef: String? = ""
)
data class Orderer(
    val name: String?,
    val phone: String?
)
data class OrderInfo(
    var cost: Double? = 0.0,
    var people: Int? = 0
)