package com.cartoonhero.privatekitchen_android.actors

import com.cartoonhero.theatre.Actor
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class InputChecker: Actor() {
    private fun actPwdStrength(password: String): Boolean {
        val pattern = Regex("^(?=.*[a-z])(?=.*[0-9].*[0-9]).{8,}\$")
        return pattern.containsMatchIn(password)
    }
    private fun actMobileNumber(number: String): Boolean {
        val pattern = Regex("^\\\\+(?:[0-9]?){6,14}[0-9]\$")
        return pattern.containsMatchIn(number)
    }
    private fun actPhoneNumber(number: String): Boolean {
        val pattern = Regex("^[+]*[(]?[0-9]{1,4}[)]?[-s./0-9]*\$")
        return pattern.containsMatchIn(number)
    }

    suspend fun bePwdStrength(password: String): Boolean {
        val actorJob = CompletableDeferred<Boolean>()
        tell {
            actorJob.complete(actPwdStrength(password))
        }
        return actorJob.await()
    }
    suspend fun beMobileNumber(number: String): Boolean {
        val actorJob = CompletableDeferred<Boolean>()
        tell {
            actorJob.complete(actMobileNumber(number))
        }
        return actorJob.await()
    }
    suspend fun bePhoneNumber(number: String): Boolean {
        val actorJob = CompletableDeferred<Boolean>()
        tell {
            actorJob.complete(actPhoneNumber(number))
        }
        return actorJob.await()
    }
}