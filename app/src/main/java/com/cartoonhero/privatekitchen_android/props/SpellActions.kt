package com.cartoonhero.privatekitchen_android.props

import com.cartoonhero.privatekitchen_android.props.obEntities.ObMenuItem
import com.cartoonhero.privatekitchen_android.props.obEntities.ObWorkstation

data class ShowWorkstation(
    val station: ObWorkstation
)
data class SaveCategoryAction(
    val title: String,
    val items: List<ObMenuItem>
)