package com.cartoonhero.privatekitchen_android.actors.generator

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class SnowFlake(private val machineId: Int) {
    private val atomicInteger = AtomicInteger(0)
    private val epoch = 1420045200000L
    private val maxMachineId = 64
    private val alphaNumericBase = 36
    private val timeStampShift = 22
    private val machineIdShift = 16

    init {
        if (machineId >= maxMachineId || machineId < 0) {
            throw IllegalArgumentException(
                "Machine Number must between 0 - ${maxMachineId - 1}"
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun parse(id: Long): SnowFlakeId {
        val ts = (id shr timeStampShift) + epoch
        val max = maxMachineId - 1L
        val machineId = (id shr machineIdShift) and max
        val i = id and max
        return SnowFlakeId(ts.toLocalDateTime(), machineId.toInt(), i.toInt())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun parse(alpha: String): SnowFlakeId {
        val id = java.lang.Long.parseLong(
            alpha.lowercase(Locale.ROOT), alphaNumericBase
        )
        return parse(id)
    }

    fun nextId(): Long {
        synchronized(this) {
            val currentTs = System.currentTimeMillis()
            val ts = currentTs - epoch
            val maxIncrement = 16384
            val max = maxIncrement - 2

            if (atomicInteger.get() >= max) {
                atomicInteger.set(0)
            }
            val i = atomicInteger.incrementAndGet()
            return (ts shl timeStampShift) or (this.machineId shl machineIdShift).toLong() or i.toLong()
        }
    }

    fun nextAlpha(): String {
        val id = nextId()
        return id.toString(alphaNumericBase)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun Long.toLocalDateTime(): LocalDateTime {
        return LocalDateTime.ofInstant(
            Instant.ofEpochMilli(this), ZoneId.systemDefault()
        )
    }
}

data class SnowFlakeId(
    val timestamp: LocalDateTime,
    val machineId: Int,
    val increment: Int
)