package com.cartoonhero.privatekitchen_android.props.entities

data class Workstation(
    val uniqueId: String?,
    val chefId: String?,
    val kitchenId: String?,
    var info: StationInfo?,
    var kitchen: StKitchen?,
    var menu: Template?
)
data class StationInfo(
    var chef: String? = ""
)
data class StKitchen(
    var info: KitchenInfo? = KitchenInfo(),
    var address: GQAddress? = GQAddress()
)
data class Template(
    val ownerId: String?,
    var pages: List<GQPage>? = listOf()
)
// MARK: - Dashboard
data class Dashboard(
    val ownerId: String?,
    var closeTime: String?,
    var steps: List<Workflow>? = listOf()
)
// MARK: - Workflow
data class Workflow(
    var step: Int? = 0,
    var name: String? = ""
)
data class Storehouse(
    val ownerId: String?,
    var items: List<MenuItem>? = listOf(),
    var options: List<GQOption>? = listOf(),
    var diningWays: List<DiningWay>? = listOf()
)
// MARK: - GQTextEditor
data class GQTextEditor(
    var titleText: LocalizedText? = LocalizedText(),
    var hintText: LocalizedText? = LocalizedText(),
    var input: String? = ""
)