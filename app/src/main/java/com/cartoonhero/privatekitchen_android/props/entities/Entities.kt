package com.cartoonhero.privatekitchen_android.props.entities

import com.cartoonhero.privatekitchen_android.props.obEntities.ObMenuItem

data class EnterListSource(
    val title: String,
    var type: SignType = SignType.Apple
)
data class WorkSideMenuVM(
    val title: String,
    val openScene: WkStTabScene
)
data class ObMuItemFile(
    val item: ObMenuItem,
    var nameText: String,
    var introText: String
)