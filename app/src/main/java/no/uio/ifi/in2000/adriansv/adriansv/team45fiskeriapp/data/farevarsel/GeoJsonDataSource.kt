package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.farevarsel

import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "GeoJsonDataSource"

class GeoJsonDataSource {
    suspend fun fetchGeoJson(): String? {
        try {
            val connection = URL("https://api.met.no/weatherapi/metalerts/2.0/current.json").openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "IN2000-StudentApp carlorr@uio.no")
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.connect()

            if (connection.responseCode == 200) {
                val rawJson = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d(TAG, "Raw API response length: ${rawJson.length}")
                val geoJson = convertToGeoJson(rawJson)
                Log.d(TAG, "Converted GeoJSON length: ${geoJson.length}")

                // Log a sample feature to understand structure
                val jsonObj = JSONObject(geoJson)
                val features = jsonObj.getJSONArray("features")
                if (features.length() > 0) {
                    val sample = features.getJSONObject(0)
                    val geometry = sample.getJSONObject("geometry")
                    val props = sample.getJSONObject("properties")
                    Log.d(TAG, "Sample feature type: ${geometry.getString("type")}")
                    Log.d(TAG, "Sample properties: event_type=${props.optString("event_type", "N/A")}, awareness_level=${props.optString("awareness_level", "N/A")}")
                } else {
                    Log.d(TAG, "No features found in GeoJSON data")
                }

                return geoJson
            } else {
                Log.e(TAG, "Server returned error code: ${connection.responseCode}")
                return null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching GeoJSON data", e)
            return null
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