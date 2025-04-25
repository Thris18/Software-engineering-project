package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.weather

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

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
        val type = jsonObject.getString("type")
        val geometry = parseGeometry(jsonObject.getJSONObject("geometry"))
        val properties = parseProperties(jsonObject.getJSONObject("properties"))
        return WeatherResponse(type = type, geometry = geometry, properties = properties)
    }
    
    private fun parseGeometry(geometryJson: JSONObject): Geometry {
        val type = geometryJson.getString("type")
        val coordinates = geometryJson.getJSONArray("coordinates").let { array ->
            List(array.length()) { array.getDouble(it) }
        }
        return Geometry(type = type, coordinates = coordinates)
    }
    
    private fun parseProperties(propertiesJson: JSONObject): Properties {
        val meta = parseMeta(propertiesJson.getJSONObject("meta"))
        val timeseries = parseTimeseries(propertiesJson.getJSONArray("timeseries"))
        return Properties(meta = meta, timeseries = timeseries)
    }
    
    private fun parseMeta(metaJson: JSONObject): Meta {
        val updated_at = metaJson.getString("updated_at")
        val units = parseUnits(metaJson.getJSONObject("units"))
        return Meta(updated_at = updated_at, units = units)
    }
    
    private fun parseUnits(unitsJson: JSONObject): Units {
        return Units(
            air_pressure_at_sea_level = unitsJson.getString("air_pressure_at_sea_level"),
            air_temperature = unitsJson.getString("air_temperature"),
            cloud_area_fraction = unitsJson.getString("cloud_area_fraction"),
            precipitation_amount = unitsJson.getString("precipitation_amount"),
            relative_humidity = unitsJson.getString("relative_humidity"),
            wind_from_direction = unitsJson.getString("wind_from_direction"),
            wind_speed = unitsJson.getString("wind_speed")
        )
    }
    
    private fun parseTimeseries(timeseriesArray: org.json.JSONArray): List<TimeSeriesEntry> {
        val timeseriesList = mutableListOf<TimeSeriesEntry>()
        
        for (i in 0 until timeseriesArray.length()) {
            val entry = timeseriesArray.getJSONObject(i)
            val time = entry.getString("time")
            val data = parseWeatherData(entry.getJSONObject("data"))
            timeseriesList.add(TimeSeriesEntry(time = time, data = data))
        }
        
        return timeseriesList
    }
    
    private fun parseWeatherData(dataJson: JSONObject): WeatherData {
        val instant = parseInstant(dataJson.getJSONObject("instant"))
        val next_1_hours = dataJson.optJSONObject("next_1_hours")?.let { parseNextHours(it) }
        val next_6_hours = dataJson.optJSONObject("next_6_hours")?.let { parseNextHours(it) }
        val next_12_hours = dataJson.optJSONObject("next_12_hours")?.let { parseNextHours(it) }
        
        return WeatherData(
            instant = instant,
            next_1_hours = next_1_hours,
            next_6_hours = next_6_hours,
            next_12_hours = next_12_hours
        )
    }
    
    private fun parseInstant(instantJson: JSONObject): Instant {
        val details = parseWeatherDetails(instantJson.getJSONObject("details"))
        return Instant(details = details)
    }
    
    private fun parseWeatherDetails(detailsJson: JSONObject): WeatherDetails {
        return WeatherDetails(
            air_pressure_at_sea_level = detailsJson.getDouble("air_pressure_at_sea_level"),
            air_temperature = detailsJson.getDouble("air_temperature"),
            cloud_area_fraction = detailsJson.getDouble("cloud_area_fraction"),
            relative_humidity = detailsJson.getDouble("relative_humidity"),
            wind_from_direction = detailsJson.getDouble("wind_from_direction"),
            wind_speed = detailsJson.getDouble("wind_speed")
        )
    }
    
    private fun parseNextHours(nextHoursJson: JSONObject): NextHours {
        val summary = parseWeatherSummary(nextHoursJson.getJSONObject("summary"))
        val details = parseNextHoursDetails(nextHoursJson.getJSONObject("details"))
        return NextHours(summary = summary, details = details)
    }
    
    private fun parseWeatherSummary(summaryJson: JSONObject): WeatherSummary {
        return WeatherSummary(symbol_code = summaryJson.getString("symbol_code"))
    }
    
    private fun parseNextHoursDetails(detailsJson: JSONObject): NextHoursDetails {
        return NextHoursDetails(
            precipitation_amount = detailsJson.optDouble("precipitation_amount", 0.0)
        )
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
    val type: String,
    val geometry: Geometry,
    val properties: Properties
)

data class Properties(
    val meta: Meta,
    val timeseries: List<TimeSeriesEntry>
)

data class TimeSeriesEntry(
    val time: String,
    val data: WeatherData
)

enum class WeatherZoomLevel {
    CITY,
    REGION,
    COUNTRY
} 