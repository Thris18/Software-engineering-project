package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib

import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib.GribData
import java.lang.Math.toDegrees
import kotlin.math.atan2

object GribDirectionUtil {

    /**
     * Beregner rotasjonsvinkel for vindpiler basert på vindretning.
     * @param windDirection Vindretning i grader (0-360) hvor vinden kommer fra
     * @return Rotasjonsvinkel i grader for piler som standard peker mot høyre
     */
    fun calculateWindRotation(windDirection: Float): Float {
        // Konverter meteorologisk retning (hvor vinden kommer fra) til pilrotasjon
        // 0° er øst, 90° er sør, 180° er vest, 270° er nord
        return (windDirection + 180) % 360
    }

    /**
     * Beregner rotasjonsvinkel for strømpiler basert på strømretning.
     * @param currentDirection Strømretning i grader (0-360) hvor strømmen går
     * @return Rotasjonsvinkel i grader for piler som standard peker mot høyre
     */
    fun calculateCurrentRotation(currentDirection: Float): Float {
        // Strømretning er allerede hvor strømmen går, så vi trenger ikke å justere
        return currentDirection
    }

    /**
     * Beregner retning fra u- og v-komponenter.
     * @param u U-komponent (øst-vest)
     * @param v V-komponent (nord-sør)
     * @return Retning i grader (0-360)
     */
    fun calculateDirectionFromUV(u: Double, v: Double): Float {
        // Beregn retning i grader (0-360)
        val direction = toDegrees(atan2(v, u))
        // Konverter til positiv vinkel
        return ((direction + 360) % 360).toFloat()
    }

    /**
     * Beregner hastighet fra u- og v-komponenter.
     * @param u U-komponent (øst-vest)
     * @param v V-komponent (nord-sør)
     * @return Hastighet
     */
    fun calculateSpeedFromUV(u: Double, v: Double): Float {
        return kotlin.math.sqrt(u * u + v * v).toFloat()
    }
} 