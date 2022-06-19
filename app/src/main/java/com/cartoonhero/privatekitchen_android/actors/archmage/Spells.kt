package com.cartoonhero.privatekitchen_android.actors.archmage

interface Spell

data class MassTeleport(
    val stuff: Any
): Spell
data class LiveScene(
    val prop: Any
): Spell
data class LiveList(
    val idx: Int,
    val prop: Any
): Spell