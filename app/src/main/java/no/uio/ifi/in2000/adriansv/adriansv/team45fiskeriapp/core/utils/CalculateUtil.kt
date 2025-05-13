package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.core.utils

import kotlin.math.atan2
import kotlin.math.sqrt

object CalculateUtil {

    fun calculateWindRotation(windDirection: Float): Float {
        // Konverter meteorologisk retning (hvor vinden kommer fra) til pilrotasjon
        // 0° er øst, 90° er sør, 180° er vest, 270° er nord
        return (windDirection + 180) % 360
    }

    fun calculateCurrentRotation(currentDirection: Float): Float {
        // Strømretning er allerede hvor strømmen går, så vi trenger ikke å justere
        return currentDirection
    }

    fun calculateDirectionFromUV(u: Double, v: Double): Float {
        // Beregn retning i grader (0-360)
        val direction = Math.toDegrees(atan2(v, u))
        // Konverter til positiv vinkel
        return ((direction + 360) % 360).toFloat()
    }

    fun calculateSpeedFromUV(u: Double, v: Double): Float {
        return sqrt(u * u + v * v).toFloat()
    }
}