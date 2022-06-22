package com.cartoonhero.privatekitchen_android.stage.scene.storage

import com.cartoonhero.privatekitchen_android.actors.archmage.Teleporter

interface StorageDirector {
    fun beShowTime(teleporter: Teleporter)
    fun beCollectParcels()
    fun beRestoreData()
    fun beUploadData(complete:(Boolean) -> Unit)
    fun beCheckPickUp(source: Set<Long>, spotId: Long, complete:(Boolean) -> Unit)
    fun beSaveCustomItem(optIds: Set<Long>)
    fun beSaveData(complete: (() -> Unit)?)
    fun beLowerCurtain()
}