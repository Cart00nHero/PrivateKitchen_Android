package com.cartoonhero.privatekitchen_android.actors.apolloApi

import com.apollographql.apollo3.ApolloClient
import com.cartoonhero.theatre.Actor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi

private val apolloSun: ApolloClient = ApolloClient.Builder()
    .serverUrl("http://54.95.141.126:8080/graphql/api")
    .build()

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class Icarus : Actor() {
}