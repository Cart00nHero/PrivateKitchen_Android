package com.cartoonhero.theatre

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class Courier(private val served: Scenario) : Actor() {
    suspend fun <T> beApply(express: T, recipient: String): Parcel<T> {
        val actorJob = CompletableDeferred<Parcel<T>>()
        tell {
            val senderName = served.javaClass.name
            val parcel = Parcel(senderName, express)
            LogisticsCenter.storeParcel(recipient, parcel)
            actorJob.complete(parcel)
        }
        return actorJob.await()
    }
    suspend fun beClaim():HashSet<Parcel<*>> {
        val actorJob = CompletableDeferred<HashSet<Parcel<*>>>()
        tell {
            val nameplate = served.javaClass.name
            val parcelSet: HashSet<Parcel<*>> = LogisticsCenter.collectParcels(nameplate)
            actorJob.complete(parcelSet)
        }
        return actorJob.await()
    }
    fun <T> beCancel(recipient: String, parcel: Parcel<T>) {
        tell {
            LogisticsCenter.cancelExpress(recipient, parcel)
        }
    }

    // ------------------------------------------------------------------------
    // MARK: - Private
    private object LogisticsCenter {
        private val warehouse:
                MutableMap<String, HashSet<Parcel<*>>> = mutableMapOf()
        fun <T> storeParcel(recipient: String, parcel: Parcel<T>) {
            val parcelSet: HashSet<Parcel<*>> =
                warehouse[recipient] ?: hashSetOf()
            if (!parcelSet.contains(parcel)) {
                parcelSet.add(parcel)
                warehouse[recipient] = parcelSet
            }
        }
        fun collectParcels(recipient: String): HashSet<Parcel<*>> {
            val parcelSet: HashSet<Parcel<*>> =
                warehouse[recipient]?.toHashSet() ?: hashSetOf()
            warehouse.remove(recipient)
            return parcelSet
        }

        fun <T> cancelExpress(recipient: String, parcel: Parcel<T>) {
            val parcelSet = warehouse[recipient]?.toHashSet()
            if (parcelSet?.contains(parcel) == true) {
                parcelSet.remove(parcel)
                if (parcelSet.isEmpty()) {
                    warehouse.remove(recipient)
                } else {
                    warehouse[recipient] = parcelSet
                }
            }
        }
    }
}