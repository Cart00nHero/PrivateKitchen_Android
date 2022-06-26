package com.cartoonhero.privatekitchen_android.stage.scene.storage

import com.cartoonhero.privatekitchen_android.actors.archmage.Teleporter
import com.cartoonhero.privatekitchen_android.props.entities.ObMuItemFile
import com.cartoonhero.privatekitchen_android.props.entities.ObOptFile
import com.cartoonhero.privatekitchen_android.props.obEntities.ObMenuItem
import com.cartoonhero.privatekitchen_android.props.obEntities.ObOption

interface StorageDirector {
    fun beShowTime(teleporter: Teleporter)
    fun beCollectParcels()
    fun beRestoreData()
    fun beUploadData(complete:(Boolean) -> Unit)
    fun beCheckPickUp(source: Set<Long>, spotId: Long, complete:(Boolean) -> Unit)
    fun beSaveCustomItem(optIds: Set<Long>)
    fun beSaveData(complete: (() -> Unit)?)
    fun beAddCustomItem(item: ObMenuItem)
    fun beEditItem(item: ObMenuItem?, complete: (() -> Unit)?)
    fun beEditOption(option: ObOption?, complete: (() -> Unit)?)
    fun beLowerCurtain()
}
interface EditItemDirector {
    fun beShowTime(teleporter: Teleporter)
    fun beCollectParcels()
    fun beUpdate(itemFile: ObMuItemFile, remove: Boolean)
    fun beLowerCurtain()
}
interface CustomizeOptDirector {
    fun beShowTime(teleporter: Teleporter)
    fun beCollectParcels()
    fun beUpdate(optFile: ObOptFile, remove: Boolean)
    fun beLowerCurtain()
}