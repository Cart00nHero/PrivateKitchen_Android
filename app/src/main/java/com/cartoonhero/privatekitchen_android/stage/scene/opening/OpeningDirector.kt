package com.cartoonhero.privatekitchen_android.stage.scene.opening

import com.cartoonhero.privatekitchen_android.actors.archmage.Teleporter

interface OpeningDirector {
    fun beShowTime(teleporter: Teleporter)
    fun beBuildSource()
    fun beSignWithApple()
    fun beSignWithGoogle()
    fun beCreateWorkstation()
    fun beLowerCurtain()
}