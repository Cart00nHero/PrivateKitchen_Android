package com.cartoonhero.privatekitchen_android.stage.scene.buildMyKitchen

import com.cartoonhero.privatekitchen_android.props.entities.KitchenInfo
import com.cartoonhero.privatekitchen_android.props.obEntities.ObStKitchen

interface BuildKitchenDirector {
    fun beCollectParcels(complete:(ObStKitchen) -> Unit)
    fun beCheckInput(phone: String, complete: (Boolean) -> Unit)
    fun beSaveKitchen(
        info: KitchenInfo, floor: String,
        complete:(() -> Unit)?
    )
}