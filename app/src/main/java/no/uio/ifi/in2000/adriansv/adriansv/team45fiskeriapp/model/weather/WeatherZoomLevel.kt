package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather

enum class WeatherZoomLevel {
    CITY,    // For zoom levels >= 12
    REGION,  // For zoom levels 8-11
    COUNTRY; // For zoom levels 4-7

    companion object {
        fun fromZoom(zoomLevel: Double): WeatherZoomLevel? {
            return when {
                zoomLevel >= 12.0 -> CITY
                zoomLevel >= 8.0 -> REGION
                zoomLevel >= 4.0 -> COUNTRY
                else -> null
            }
        }
    }
} 