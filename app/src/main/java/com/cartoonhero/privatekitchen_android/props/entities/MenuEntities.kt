package com.cartoonhero.privatekitchen_android.props.entities

data class GQMenu(
    val ownerId: String?,
    var deadline: String? = "",
    var titleText: LocalizedText? = LocalizedText(),
    var pages:List<GQPage>? = listOf()
)
data class GQPage(
    var pagination: Int? = 0,
    var categories: List<GQCategory>? = listOf()
)
data class GQCategory(
    val spotId: String?,
    var sequence: Int? = 0,
    var titleText: LocalizedText? = LocalizedText(),
    var items: List<MenuItem>? = listOf()
)
data class MenuItem(
    val spotId: String?,
    var sequence: Int? = 0,
    var nameText: LocalizedText? = LocalizedText(),
    var introText: LocalizedText? = LocalizedText(),
    var price: Double? = 0.0,
    var photo: String? = "",
    var quota: Int? = 0,
    var customizations: List<GQOption>? = listOf()
)
data class GQOption(
    val spotId: String?,
    var price: Double? = 0.0,
    var titleText: LocalizedText? = LocalizedText()
)