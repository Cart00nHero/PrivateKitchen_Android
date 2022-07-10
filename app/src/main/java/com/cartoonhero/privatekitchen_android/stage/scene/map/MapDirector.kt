package com.cartoonhero.privatekitchen_android.stage.scene.map

import com.cartoonhero.privatekitchen_android.actors.archmage.Teleporter

interface MapDirector {
    fun beShowTime(teleport: Teleporter)
    fun beNewLocation()
    fun beLowerCurtain()
}