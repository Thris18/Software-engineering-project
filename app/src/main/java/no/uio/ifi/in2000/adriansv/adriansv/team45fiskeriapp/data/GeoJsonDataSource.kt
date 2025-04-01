package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class GeoJsonDataSource {
    companion object {
        private const val API_URL = "https://api.met.no/weatherapi/metalerts/2.0/current.json"
    }

    suspend fun fetchGeoJson(): String? = withContext(Dispatchers.IO) {
        try {
            val connection = URL(API_URL).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "IN2000-StudentApp")
            connection.connect()

            if (connection.responseCode == 200) {
                val rawJson = connection.inputStream.bufferedReader().use { it.readText() }
                convertToGeoJson(rawJson)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun convertToGeoJson(rawJson: String): String {
        val jsonObject = JSONObject(rawJson)
        val featuresArray = jsonObject.getJSONArray("features")
        val geoJson = JSONObject()
        geoJson.put("type", "FeatureCollection")
        geoJson.put("features", featuresArray)
        return geoJson.toString()
    }
}