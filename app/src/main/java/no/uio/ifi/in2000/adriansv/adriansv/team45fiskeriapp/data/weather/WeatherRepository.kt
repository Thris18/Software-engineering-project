package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.weather

import android.util.Log
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather.LocationWeather
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather.WeatherZoomLevel

private const val TAG = "WeatherRepository"

class WeatherRepository(private val weatherDataSource: WeatherDataSource) {
    
    suspend fun getWeatherForLocation(
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
                
                val locationWeather = LocationWeather(
                    latitude = latitude,
                    longitude = longitude,
                    temperature = firstTimeSeries.weatherInfo.temperature,
                    symbolCode = firstTimeSeries.weatherInfo.weatherIcon.getAssetPath(),
                    zoomLevel = zoomLevel
                )
                Log.d(TAG, "Successfully mapped weather data: $locationWeather")
                locationWeather
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get weather for location", e)
            Result.failure(e)
        }
    }
    
    companion object {
        fun shouldShowWeather(zoomLevel: Double): Boolean {
            val should = WeatherDataSource.getWeatherZoomLevel(zoomLevel) != null
            Log.d(TAG, "Should show weather for zoom $zoomLevel: $should")
            return should
        }
        
        fun getWeatherZoomLevel(zoomLevel: Double): no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.weather.WeatherZoomLevel? {
            val level = WeatherDataSource.getWeatherZoomLevel(zoomLevel)
            Log.d(TAG, "Weather zoom level for $zoomLevel: $level")
            return level
        }
    }
} 