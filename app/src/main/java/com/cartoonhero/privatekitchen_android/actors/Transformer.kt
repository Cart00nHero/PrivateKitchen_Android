package com.cartoonhero.privatekitchen_android.actors

import com.cartoonhero.theatre.Actor
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class Transformer: Actor() {
    suspend inline fun <reified T1,reified T2> beTransfer(from: T1): T2? {
        val actorJob = CompletableDeferred<T2?>()
        tell {
            val fromAdapter =
                Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                    .adapter(T1::class.java)
            val json: String = fromAdapter.toJson(from)
            val toAdapter =
                Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                    .adapter(T2::class.java)
            val entity: T2? = toAdapter.fromJson(json)
            actorJob.complete(entity)
        }
        return actorJob.await()
    }
    suspend inline fun <reified T> beToEntity(json: String): T? {
        val actorJob = CompletableDeferred<T?>()
        tell {
            val jsonAdapter =
                Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                    .adapter(T::class.java)
            val entity: T? = jsonAdapter.fromJson(json)
            actorJob.complete(entity)
        }
        return actorJob.await()
    }
    suspend inline fun <reified T> beToJson(entity: T): String {
        val actorJob = CompletableDeferred<String>()
        tell {
            val jsonAdapter =
                Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                    .adapter(T::class.java)
            val jsonStr: String = jsonAdapter.toJson(entity)
            actorJob.complete(jsonStr)
        }
        return actorJob.await()
    }
    suspend inline fun <reified T> beJsonTo(json: String): Map<String,T>? {
        val actorJob = CompletableDeferred<Map<String,T>?>()
        tell {
            val type = Types.newParameterizedType(Map::class.java, String::class.java, T::class.java)
            val jsonAdapter: JsonAdapter<Map<String, T>> = Moshi.Builder().build().adapter(type)
            val map: Map<String,T>? = jsonAdapter.fromJson(json)
            actorJob.complete(map)
        }
        return actorJob.await()
    }
    suspend inline fun <reified T> beMapTo(map: Map<String,T>): String {
        val actorJob = CompletableDeferred<String>()
        tell {
            val type = Types.newParameterizedType(Map::class.java, String::class.java, T::class.java)
            val jsonAdapter: JsonAdapter<Map<String, T>> = Moshi.Builder().build().adapter(type)
            val jsonStr: String = jsonAdapter.toJson(map)
            actorJob.complete(jsonStr)
        }
        return actorJob.await()
    }
    suspend inline fun <reified T1, reified T2> beMapToEntity(map: Map<String,T1>): T2? {
        val actorJob = CompletableDeferred<T2?>()
        tell {
            val type = Types.newParameterizedType(
                Map::class.java, String::class.java, T1::class.java
            )
            val fromAdapter: JsonAdapter<Map<String, T1>> = Moshi.Builder().build().adapter(type)
            val json: String = fromAdapter.toJson(map)
            val toAdapter =
                Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                    .adapter(T2::class.java)
            val entity: T2? = toAdapter.fromJson(json)
            actorJob.complete(entity)
        }
        return actorJob.await()
    }
    suspend inline fun <reified T1, reified T2> beEntityTo(entity: T1): Map<String,T2>? {
        val actorJob = CompletableDeferred<Map<String,T2>?>()
        tell {
            val fromAdapter =
                Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                    .adapter(T1::class.java)
            val json: String = fromAdapter.toJson(entity)
            val type = Types.newParameterizedType(
                Map::class.java, String::class.java, T2::class.java
            )
            val toAdapter: JsonAdapter<Map<String, T2>> = Moshi.Builder().build().adapter(type)
            val map: Map<String,T2>? = toAdapter.fromJson(json)
            actorJob.complete(map)
        }
        return actorJob.await()
    }
}