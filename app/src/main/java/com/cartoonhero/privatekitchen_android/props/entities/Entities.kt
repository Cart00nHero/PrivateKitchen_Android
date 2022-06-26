package com.cartoonhero.privatekitchen_android.props.entities

import com.cartoonhero.privatekitchen_android.props.obEntities.ObMenuItem
import com.cartoonhero.privatekitchen_android.props.obEntities.ObOption

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
data class ObOptFile(
    val option: ObOption,
    var titleText: String = ""
)
data class UnPickedItems(
    val unPicked: List<ObMenuItem>
)