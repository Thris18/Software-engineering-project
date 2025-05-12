package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib

object WaveOverlayUtil {
    fun getFogImageName(value: Double, threshold: Double): String {
        val ratio = value / threshold
        val fog = when {
            ratio < 1.4 -> "blue"
            ratio < 1.9 -> "yellow"
            else -> "red"
        }
        android.util.Log.d("WaveOverlayUtil", "Bølgehøyde: $value, threshold: $threshold, ratio: $ratio, bilde: $fog")
        return fog
    }
} 