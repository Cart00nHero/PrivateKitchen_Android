package com.cartoonhero.privatekitchen_android.stage.scene.main

import com.cartoonhero.privatekitchen_android.actors.archmage.Teleporter

interface MainDirector {
    fun showTime()
    fun beInitDb()
    fun beTestMethod()
    fun beTestLiveData(complete:(String) -> Unit)
    fun beTestRedux(portal: Teleporter)
}