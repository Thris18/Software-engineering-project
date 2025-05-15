package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.core.utils

import kotlin.math.atan2
import kotlin.math.sqrt

object CalculateUtil {

    fun calculateWindRotation(windDirection: Float): Float {
        return (windDirection + 180) % 360
    }

    fun calculateCurrentRotation(currentDirection: Float): Float {
        return currentDirection
    }

    fun calculateDirectionFromUV(u: Double, v: Double): Float {
        val direction = Math.toDegrees(atan2(v, u))
        return ((direction + 360) % 360).toFloat()
    }

    fun calculateSpeedFromUV(u: Double, v: Double): Float {
        return sqrt(u * u + v * v).toFloat()
    }
}