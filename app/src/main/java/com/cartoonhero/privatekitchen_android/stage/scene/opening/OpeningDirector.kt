package com.cartoonhero.privatekitchen_android.stage.scene.opening

import android.content.Context
import com.cartoonhero.privatekitchen_android.props.entities.EnterListSource

interface OpeningDirector {
    fun beShowTime()
    fun beSubscribeListSource(
        subscriber: (List<EnterListSource>) -> Unit
    )
    fun beBuildSource(complete:(List<EnterListSource>) -> Unit)
    fun beSignWithApple()
    fun beSignWithFaceBook()
    fun beCreateWorkstation()
    fun beLowerCurtain()
}