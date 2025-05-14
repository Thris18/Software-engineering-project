package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.weather

import android.util.Log
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather.LocationWeather
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather.WeatherZoomLevel

private const val TAG = "WeatherRepository"

interface WeatherRepository {
    suspend fun getWeatherForLocation(latitude: Double, longitude: Double, zoomLevel: WeatherZoomLevel): Result<LocationWeather>

    class WeatherRepositoryImpl(private val weatherDataSource: WeatherDataSource) : WeatherRepository {

        override suspend fun getWeatherForLocation(
            latitude: Double,
            longitude: Double,
            zoomLevel: WeatherZoomLevel
        ): Result<LocationWeather> {
            Log.d(TAG, "Getting weather for location: lat=$latitude, lon=$longitude, zoom=$zoomLevel")
            return try {
                val response = weatherDataSource.fetchWeather(latitude, longitude, zoomLevel)

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
                    Log.d(TAG, "Successfully mapped weather data: $locationWeather")
                    locationWeather
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get weather for location", e)
                Result.failure(e)
            }
        }
    }
}
