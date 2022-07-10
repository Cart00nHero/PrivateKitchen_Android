package com.cartoonhero.theatre

data class Parcel<T>(
    val sender: String,
    val content: T
)
