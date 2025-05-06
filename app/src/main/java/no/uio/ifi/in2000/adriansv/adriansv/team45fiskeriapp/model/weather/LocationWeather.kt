package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather

data class LocationWeather(
    val latitude: Double,
    val longitude: Double,
    val temperature: Double,
    val symbolCode: String,
    val zoomLevel: WeatherZoomLevel?,
    val windSpeed: Double = 0.0,
    val windDirection: Double = 0.0,
    val cloudAreaFraction: Double = 0.0,
    val precipitationAmount: Double = 0.0,
    val airPressure: Double = 0.0,
    val timeseries: List<TimeSeriesEntry> = emptyList(),
)

