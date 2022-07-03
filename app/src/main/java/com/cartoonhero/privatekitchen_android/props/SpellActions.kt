package com.cartoonhero.privatekitchen_android.props

import com.cartoonhero.privatekitchen_android.props.obEntities.ObWorkstation

data class ShowWorkstation(
    val station: ObWorkstation
)

data class EditMenuComplete(
    val editing: Boolean = false
)