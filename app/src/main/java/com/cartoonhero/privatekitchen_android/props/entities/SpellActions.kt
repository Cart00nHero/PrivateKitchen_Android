package com.cartoonhero.privatekitchen_android.props.entities

import com.cartoonhero.privatekitchen_android.props.obEntities.ObMenuItem
import com.cartoonhero.privatekitchen_android.props.obEntities.ObOption
import graphqlApollo.operation.type.InputOrderItem

data class ModifyItemAction(
    val modifyType: ModifyType,
    val item: ObMenuItem
)
data class ModifyOptionAction(
    val modifyType: ModifyType,
    val option: ObOption
)
data class CustomizedItem(
    val selected: ObMenuItem,
    val chosenIds: Set<Long>
)
data class DisplayOrderData(
    val inputItems: List<InputOrderItem>,
    val menuItems: List<MenuItem>
)
data class CalculateCustom(
    val index: Int,
    val odrItem: InputOrderItem
)
data class UpdateTotalChosen(
    val spotId: String,
    val total: Int
)