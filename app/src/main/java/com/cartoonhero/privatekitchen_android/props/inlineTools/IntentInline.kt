package com.cartoonhero.privatekitchen_android.props.inlineTools
import android.app.Activity
import android.content.Intent
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

inline fun <reified T : Activity> Activity.startActivity(inInitializer: Intent.() -> Unit) {
  startActivity(
    Intent(this,T::class.java).apply(inInitializer)
  )
}

inline fun <reified T> String.toEntity(): T? {
  val jsonAdapter =
    Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
      .adapter(T::class.java)
  return jsonAdapter.fromJson(this)
}