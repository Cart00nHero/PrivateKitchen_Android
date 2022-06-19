package com.cartoonhero.privatekitchen_android.actors.archmage

data class AppState(
     val spell: Spell = LiveScene(0)
)
fun mageReducer(state: AppState, action: Any) = AppState(
     spell = enchantReducer(state.spell, action)
)
fun enchantReducer(state: Spell, action: Any) =
     when(action) {
          is Spell -> action
          else -> state
     }
