package com.cartoonhero.privatekitchen_android.props.entities

import com.cartoonhero.privatekitchen_android.props.obEntities.ObMenuItem
import com.cartoonhero.privatekitchen_android.props.obEntities.ObOption

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