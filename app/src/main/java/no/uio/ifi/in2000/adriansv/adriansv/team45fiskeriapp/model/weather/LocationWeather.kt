package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather

data class LocationWeather(
    val latitude: Double,
    val longitude: Double,
    val temperature: Double,
    val symbolCode: String,
    val zoomLevel: no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather.WeatherZoomLevel?
) 