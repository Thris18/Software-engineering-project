package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib

object WaveOverlayUtil {
    // Hardkodede terskler for eksempel
    private const val BLUE_THRESHOLD = 0.1
    private const val YELLOW_THRESHOLD = 0.3

    fun getFogImageName(value: Double): String {
        val ratio = value / YELLOW_THRESHOLD
        val fog = when {
            ratio < 1.4 -> "blue"
            ratio < 1.9 -> "yellow"
            else -> "red"
        }
        android.util.Log.d("WaveOverlayUtil", "Bølgehøyde: $value, ratio: $ratio, bilde: $fog")
        return fog
    }
} 