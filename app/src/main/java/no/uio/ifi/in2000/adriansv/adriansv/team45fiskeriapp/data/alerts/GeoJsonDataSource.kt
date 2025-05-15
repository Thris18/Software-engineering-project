package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.alerts

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

private const val TAG = "GeoJsonDataSource"

class GeoJsonDataSource {
    fun fetchGeoJson(): String? {
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

                try {
                    val rawJsonObj = JSONObject(rawJson)
                    Log.d(TAG, "Raw JSON keys: ${rawJsonObj.keys().asSequence().toList()}")
                    if (rawJsonObj.has("features")) {
                        val rawFeatures = rawJsonObj.getJSONArray("features")
                        Log.d(TAG, "Number of raw features: ${rawFeatures.length()}")
                        if (rawFeatures.length() > 0) {
                            val sampleRaw = rawFeatures.getJSONObject(0)
                            Log.d(TAG, "Raw feature keys: ${sampleRaw.keys().asSequence().toList()}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error analyzing raw JSON", e)
                }
                
                val geoJson = convertToGeoJson(rawJson)
                Log.d(TAG, "Converted GeoJSON length: ${geoJson.length}")

                val jsonObj = JSONObject(geoJson)
                val features = jsonObj.getJSONArray("features")
                Log.d(TAG, "Number of features in converted GeoJSON: ${features.length()}")
                
                if (features.length() > 0) {
                    val sample = features.getJSONObject(0)
                    val geometry = sample.getJSONObject("geometry")
                    val props = sample.getJSONObject("properties")
                    Log.d(TAG, "Sample feature type: ${geometry.getString("type")}")
                    Log.d(TAG, "Sample geometry: $geometry")
                    Log.d(TAG, "Sample properties keys: ${props.keys().asSequence().toList()}")
                    Log.d(TAG, "Sample properties: $props")
                    Log.d(TAG, "Sample eventAwarenessName: ${props.optString("eventAwarenessName", "N/A")}")
                    Log.d(TAG, "Sample severity: ${props.optString("severity", "N/A")}")
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

        val modifiedFeatures = JSONArray()
        for (i in 0 until featuresArray.length()) {
            val feature = featuresArray.getJSONObject(i)
            val properties = feature.getJSONObject("properties")
            
            // Ensure we have the required properties for the icon matching
            if (!properties.has("eventAwarenessName")) {
                properties.put("eventAwarenessName", properties.optString("event_type", "Unknown"))
            }
            if (!properties.has("severity")) {
                val awarenessLevel = properties.optString("awareness_level", "")
                    .lowercase(Locale.ROOT)
                val severity = when {
                    awarenessLevel.contains("severe") -> "Severe"
                    awarenessLevel.contains("moderate") -> "Moderate"
                    awarenessLevel.contains("minor") -> "Minor"
                    else -> "Minor"
                }
                properties.put("severity", severity)
            }
            
            modifiedFeatures.put(feature)
        }
        
        val geoJson = JSONObject()
        geoJson.put("type", "FeatureCollection")
        geoJson.put("features", modifiedFeatures)
        return geoJson.toString()
    }
}