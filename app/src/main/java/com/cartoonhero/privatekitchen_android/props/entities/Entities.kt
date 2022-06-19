package com.cartoonhero.privatekitchen_android.props.entities

data class EnterListSource(
    val title: String,
    var type: SignType = SignType.Apple
)
data class WorkSideMenuVM(
    val title: String,
    val openScene: WkStTabScene
)