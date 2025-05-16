package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.weather

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "WeatherDataSource"

class WeatherDataSource {
    private val parser = WeatherResponseParser()
    
    suspend fun fetchWeather(latitude: Double, longitude: Double): Result<WeatherResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val baseUrl = "https://api.met.no/weatherapi/locationforecast/2.0/compact"
                val url = "$baseUrl?lat=$latitude&lon=$longitude"
                val connection = URL(url).openConnection() as HttpURLConnection

                connection.setRequestProperty("User-Agent", "FiskeriApp/1.0")
                
                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val jsonObject = JSONObject(response)

                    Result.success(parser.parseJsonToWeatherResponse(jsonObject))
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
}