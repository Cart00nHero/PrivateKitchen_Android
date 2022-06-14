package com.cartoonhero.privatekitchen_android.stage.scene.workstation

import com.cartoonhero.privatekitchen_android.props.entities.WkStTabScene

interface WorkStDirector {
    fun beShowTime()
    fun beSubscribeSceneContent(subscriber: (WkStTabScene) -> Unit)
    fun bePrepare(scene: WkStTabScene, complete:(() -> Unit)?)
    fun bePackWkStParcel(recipient: String, complete:(() -> Unit)?)
    fun beLowerCurtain()
}