package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.weather

import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather.LocationWeather
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather.WeatherZoomLevel

interface WeatherRepository {
    suspend fun getWeatherForLocation(latitude: Double, longitude: Double, zoomLevel: WeatherZoomLevel): Result<LocationWeather>

    class WeatherRepositoryImpl(private val weatherDataSource: WeatherDataSource) : WeatherRepository {

        override suspend fun getWeatherForLocation(
            latitude: Double,
            longitude: Double,
            zoomLevel: WeatherZoomLevel
        ): Result<LocationWeather> {

            return try {
                val response = weatherDataSource.fetchWeather(latitude, longitude)

                response.map { weatherResponse ->
                    val firstTimeSeries = weatherResponse.properties.timeseries.firstOrNull()
                        ?: throw Exception("No weather data available")

                    val instantDetails = firstTimeSeries.data.instant.details
                    val next1Hours = firstTimeSeries.data.next1hours

                    val locationWeather = LocationWeather(
                        latitude = latitude,
                        longitude = longitude,
                        temperature = instantDetails.airTemperature,
                        symbolCode = next1Hours?.summary?.symbolCode ?: "cloudy",
                        zoomLevel = zoomLevel,
                        windSpeed = instantDetails.windSpeed,
                        windDirection = instantDetails.windFromDirection,
                        cloudAreaFraction = instantDetails.cloudAreaFraction,
                        precipitationAmount = next1Hours?.details?.precipitationAmount ?: 0.0,
                        airPressure = instantDetails.airPressureAtSeaLevel,
                        timeseries = weatherResponse.properties.timeseries
                    )
                    locationWeather
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
