package com.cartoonhero.privatekitchen_android.props.inlineTools

import android.annotation.SuppressLint
import android.content.SharedPreferences

@SuppressLint("CommitPrefEdits")
inline fun SharedPreferences.applyEdit(
    func: SharedPreferences.Editor.() -> SharedPreferences.Editor) {
    this.edit().func().apply()
}