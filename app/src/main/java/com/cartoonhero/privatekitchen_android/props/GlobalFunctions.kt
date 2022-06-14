package com.cartoonhero.privatekitchen_android.props

import android.annotation.SuppressLint
import android.content.Context
import com.cartoonhero.privatekitchen_android.actors.generator.SnowFlake

@SuppressLint("StaticFieldLeak")
lateinit var mainContext: Context
const val sharedStorage: String = "Private_Kitchen"
fun generateSpotId(): Long {
    val snowFlake = SnowFlake(1)
    return snowFlake.nextId()
}