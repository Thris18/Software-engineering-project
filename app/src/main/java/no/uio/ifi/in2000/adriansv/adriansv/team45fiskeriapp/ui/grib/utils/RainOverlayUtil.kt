package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib.utils

object RainOverlayUtil {
    fun getFogImageName(value: Double, threshold: Double): String {
        val ratio = value / threshold
        val fog = when {
            ratio < 1.4 -> "blue"
            ratio < 1.9 -> "yellow"
            else -> "red"
        }
        return fog
    }
}