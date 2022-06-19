package com.cartoonhero.privatekitchen_android.stage.scene.workstation

import com.cartoonhero.privatekitchen_android.actors.archmage.Teleporter
import com.cartoonhero.privatekitchen_android.props.entities.WkStTabScene
import com.cartoonhero.privatekitchen_android.props.obEntities.ObWorkstation

interface WorkStDirector {
    fun beShowTime(teleporter: Teleporter)
    fun bePrepare(scene: WkStTabScene, complete:(() -> Unit)?)
    fun bePackWkStParcel(recipient: String, complete:(() -> Unit)?)
    fun beLowerCurtain()
}
interface KitchenDirector {
    fun beCollectParcels(complete: (ObWorkstation) -> Unit)
    fun beSendParcels(
        recipient: String, parcel: ObWorkstation,
        complete: (() -> Unit)?
    )
}