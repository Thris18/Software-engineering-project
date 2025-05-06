package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib

object RainOverlayUtil {
    // Hardkodede terskler for eksempel
    private const val BLUE_THRESHOLD = 1.0
    private const val YELLOW_THRESHOLD = 5.0

    fun getFogImageName(value: Double): String {
        val ratio = value / YELLOW_THRESHOLD
        val fog = when {
            ratio < 1.4 -> "blue"
            ratio < 1.9 -> "yellow"
            else -> "red"
        }
        android.util.Log.d("RainOverlayUtil", "Nedbørverdi: $value, ratio: $ratio, bilde: $fog")
        return fog
    }
} 