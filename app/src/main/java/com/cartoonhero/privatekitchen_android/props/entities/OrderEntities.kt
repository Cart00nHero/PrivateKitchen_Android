package com.cartoonhero.privatekitchen_android.props.entities

// MARK: - OrderForm
data class OrderForm(
    val uniqueId: String?,
    val kitchenId: String?,
    val userId: String?,
    var setUp: String? = "",
    var state: Int?,
    var arrival: String?,
    var paid: Boolean? = false,
    val orderer: Orderer?,
    var info: OrderInfo?,
    var diningWay: DiningWay?,
    var items: List<OrderItem>? = listOf()
)
// MARK: - Orderer
data class Orderer(
    val name: String?,
    val phone: String?
)
// MARK: - OrderInfo
data class OrderInfo(
    var cost: Double? = 0.0,
    var people: Int? = 0
)
// MARK: - DiningWay
data class DiningWay(
    var spotId: String? = "",
    var sequence: Int? = 0,
    var option: LocalizedText? = LocalizedText(),
    var editor: GQTextEditor? = GQTextEditor()
)
// MARK: - OrderItem
data class OrderItem(
    var pagination: Int? = 0,
    var categoryId: String?,
    var quantity: Int? = 0,
    val item: MenuItem,
    var customize: List<Choice>? = listOf()
)
// MARK: - Choice
data class Choice(
    var amount: Int? = 0,
    var cost: Double? = 0.0,
    var choices: List<GQOption>? = listOf()
)