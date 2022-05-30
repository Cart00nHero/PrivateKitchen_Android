package com.cartoonhero.privatekitchen_android.props

import com.cartoonhero.privatekitchen_android.actors.generator.SnowFlake

fun generateSpotId(): Long {
    val snowFlake = SnowFlake(1)
    return snowFlake.nextId()
}