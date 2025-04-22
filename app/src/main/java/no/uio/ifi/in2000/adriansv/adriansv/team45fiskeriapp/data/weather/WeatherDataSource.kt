package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.weather

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar

private const val TAG = "WeatherDataSource"
private const val BASE_URL = "https://api.met.no/weatherapi/locationforecast/2.0/compact"

class WeatherDataSource {
    suspend fun fetchWeather(latitude: Double, longitude: Double, zoomLevel: no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather.WeatherZoomLevel): Result<WeatherResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "$BASE_URL?lat=$latitude&lon=$longitude"
                val connection = URL(url).openConnection() as HttpURLConnection
                
                // Sett User-Agent header som kreves av MET API
                connection.setRequestProperty("User-Agent", "FiskeriApp/1.0")
                
                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val jsonObject = JSONObject(response)
                    
                    Log.d(TAG, "Weather fetched for location: lat=$latitude, lon=$longitude, zoomLevel=$zoomLevel")
                    Result.success(parseJsonToWeatherResponse(jsonObject))
                } else {
                    val errorMessage = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                    Log.e(TAG, "Failed to fetch weather. Response code: ${connection.responseCode}, Error: $errorMessage")
                    Result.failure(Exception("Failed to fetch weather: ${connection.responseCode}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching weather", e)
                Result.failure(e)
            }
        }
    }
    
    private fun parseJsonToWeatherResponse(jsonObject: JSONObject): WeatherResponse {
        val properties = parseProperties(jsonObject.getJSONObject("properties"))
        return WeatherResponse(properties = properties)
    }
    
    private fun parseProperties(propertiesJson: JSONObject): Properties {
        val timeseries = propertiesJson.getJSONArray("timeseries")
        val timeseriesList = mutableListOf<TimeSeriesEntry>()
        
        for (i in 0 until timeseries.length()) {
            val entry = timeseries.getJSONObject(i)
            val time = entry.getString("time")
            val data = entry.getJSONObject("data")
            
            // Parse instant data
            val instant = data.getJSONObject("instant")
            val details = instant.getJSONObject("details")
            val temperature = details.getDouble("air_temperature")
            
            // Parse next 1 hour data if available
            val next1Hours = data.optJSONObject("next_1_hours")
            val weatherSymbol = next1Hours?.getJSONObject("summary")?.getString("symbol_code") ?: "cloudy"
            
            // Get current hour for icon selection
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = parseTimeString(time)
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            
            // Create WeatherInfo object
            val weatherInfo = WeatherInfo(
                temperature = temperature,
                weatherIcon = WeatherIcon.fromWeatherCode(weatherSymbol, hour),
                description = getWeatherDescription(weatherSymbol)
            )
            
            timeseriesList.add(
                TimeSeriesEntry(
                    time = time,
                    weatherInfo = weatherInfo
                )
            )
        }
        
        return Properties(timeseries = timeseriesList)
    }

    private fun parseTimeString(timeString: String): Long {
        return try {
            // Først splitt på T for å skille dato og tid
            val (dateStr, timeStr) = timeString.split("T")
            
            // Splitt datoen på bindestrek
            val dateParts = dateStr.split("-")
            val year = dateParts[0].toInt()
            val month = dateParts[1].toInt()
            val day = dateParts[2].toInt()
            
            // Splitt tiden på kolon og fjern Z
            val timeParts = timeStr.replace("Z", "").split(":")
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()
            
            val calendar = Calendar.getInstance()
            calendar.set(year, month - 1, day, hour, minute, 0)
            calendar.timeInMillis
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing time string: $timeString", e)
            System.currentTimeMillis()
        }
    }

    private fun getWeatherDescription(symbolCode: String): String {
        return when (symbolCode.lowercase()) {
            "clearsky" -> "Klart vær"
            "fair", "fair_day" -> "Lettskyet"
            "partlycloudy" -> "Delvis skyet"
            "cloudy" -> "Overskyet"
            "fog" -> "Tåke"
            "drizzle" -> "Yr"
            "lightrain", "lightrainshowers" -> "Lett regn"
            "rain" -> "Regn"
            "heavyrain" -> "Kraftig regn"
            "heavyrainshowers" -> "Kraftige regnbyger"
            "lightsleet" -> "Lett sludd"
            "sleet" -> "Sludd"
            "heavysleet" -> "Kraftig sludd"
            "lightsnow" -> "Lett snø"
            "snow" -> "Snø"
            "heavysnow" -> "Kraftig snø"
            "lightrainandthunder" -> "Lett regn og torden"
            "rainandthunder" -> "Regn og torden"
            "heavyrainandthunder" -> "Kraftig regn og torden"
            "lightsleetandthunder" -> "Lett sludd og torden"
            "sleetandthunder" -> "Sludd og torden"
            "lightsnowandthunder" -> "Lett snø og torden"
            "snowandthunder" -> "Snø og torden"
            "heavysnowandthunder" -> "Kraftig snø og torden"
            else -> "Ukjent vær"
        }
    }
    
    companion object {
        // Zoom-nivåer for når vi skal vise vær
        const val CITY_ZOOM_LEVEL = 12.0    // Viser vær for byer når zoom er større enn 12
        const val REGION_ZOOM_LEVEL = 8.0   // Viser vær for regioner når zoom er større enn 8
        const val COUNTRY_ZOOM_LEVEL = 5.0  // Viser vær for land når zoom er større enn 5
        
        fun getWeatherZoomLevel(zoomLevel: Double): WeatherZoomLevel? {
            return when {
                zoomLevel >= CITY_ZOOM_LEVEL -> WeatherZoomLevel.CITY
                zoomLevel >= REGION_ZOOM_LEVEL -> WeatherZoomLevel.REGION
                zoomLevel >= COUNTRY_ZOOM_LEVEL -> WeatherZoomLevel.COUNTRY
                else -> null // Ikke vis vær når zoom er for langt ute
            }
        }
    }
}

// Data classes for å representere værdata
data class WeatherResponse(
    val properties: Properties
)

data class Properties(
    val timeseries: List<TimeSeriesEntry>
)

data class TimeSeriesEntry(
    val time: String,
    val weatherInfo: WeatherInfo
)

enum class WeatherZoomLevel {
    CITY,
    REGION,
    COUNTRY
} 