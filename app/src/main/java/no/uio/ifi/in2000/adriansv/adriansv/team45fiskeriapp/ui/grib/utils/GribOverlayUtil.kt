package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib.utils

import android.content.Context
import android.graphics.Color
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.core.utils.CalculateUtil
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib.GribData
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object GribOverlayUtil {
    private const val PREFS_NAME = "GribThresholds"
    private const val KEY_WIND = "wind_threshold"
    private const val KEY_WAVE = "wave_threshold"
    private const val KEY_CURRENT = "current_threshold"
    private const val KEY_RAIN = "rain_threshold"

    // Standard threshholds as fallback
    private val defaultThresholds = mapOf(
        "wind" to 2.0,
        "wave" to 0.3,
        "strom" to 0.1,
        "rain" to 0.5
    )

    // Dynamic thresholds
    var currentThresholds = defaultThresholds.toMutableMap()

    // Initialize with default values
    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        currentThresholds["wind"] = prefs.getFloat(KEY_WIND, defaultThresholds["wind"]!!.toFloat()).toDouble()
        currentThresholds["wave"] = prefs.getFloat(KEY_WAVE, defaultThresholds["wave"]!!.toFloat()).toDouble()
        currentThresholds["strom"] = prefs.getFloat(KEY_CURRENT, defaultThresholds["strom"]!!.toFloat()).toDouble()
        currentThresholds["rain"] = prefs.getFloat(KEY_RAIN, defaultThresholds["rain"]!!.toFloat()).toDouble()
    }

    fun updateThresholds(
        context: Context,
        windThreshold: Float,
        waveThreshold: Float,
        currentThreshold: Float,
        precipitationThreshold: Float
    ) {
        currentThresholds["wind"] = windThreshold.toDouble()
        currentThresholds["wave"] = waveThreshold.toDouble()
        currentThresholds["strom"] = currentThreshold.toDouble()
        currentThresholds["rain"] = precipitationThreshold.toDouble()

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
            putFloat(KEY_WIND, windThreshold)
            putFloat(KEY_WAVE, waveThreshold)
            putFloat(KEY_CURRENT, currentThreshold)
            putFloat(KEY_RAIN, precipitationThreshold)
            apply()
        }
    }

    private fun getColor(type: String, value: Double): Int {
        val threshold = currentThresholds[type] ?: defaultThresholds[type] ?: 0.0
        val normalizedValue = (value - threshold).coerceAtLeast(0.0)
        val maxValue = when (type) {
            "wind" -> 30.0
            "wave" -> 10.0
            "strom" -> 5.0
            "rain" -> 20.0
            else -> 1.0
        }
        val normalizedThreshold = maxValue - threshold
        val intensity = (normalizedValue / normalizedThreshold).coerceIn(0.0, 1.0)

        return when (type) {
            "wind" -> {
                val red = (255 * intensity).toInt()
                val green = (255 * (1 - intensity)).toInt()
                Color.rgb(red, green, 0)
            }
            "wave" -> {
                val blue = (255 * intensity).toInt()
                val green = (255 * (1 - intensity)).toInt()
                Color.rgb(0, green, blue)
            }
            "strom" -> {
                val red = (255 * intensity).toInt()
                val blue = (255 * (1 - intensity)).toInt()
                Color.rgb(red, 0, blue)
            }
            "rain" -> {
                val blue = (255 * intensity).toInt()
                val red = (255 * (1 - intensity)).toInt()
                Color.rgb(red, 0, blue)
            }
            else -> Color.TRANSPARENT
        }
    }

    // Radius for fog
    private fun getRadius(type: String, value: Double): Double {
        val threshold = currentThresholds[type] ?: defaultThresholds[type] ?: return 12.0
        val base = 12.0
        val extra = ((value - threshold) * 2.0).coerceAtLeast(0.0)
        return base + extra
    }



    // Help function for calculating distance between two points
    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Radius of Earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }


    fun gribDataToFeatureCollection(
        gribData: GribData,
        type: String,
        icon: String,
        minDistanceKm: Double = 10.0
    ): String {
        val features = mutableListOf<String>()
        val threshold = currentThresholds[type] ?: 0.0
        val width = gribData.width
        val height = gribData.height
        var lastLat = Double.NaN
        var lastLon = Double.NaN

        // Use different minimum distances based on type
        val typeMinDistanceKm = when (type) {
            "wind" -> 20.0  // Increased distance for wind icons
            else -> minDistanceKm
        }

        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val value = gribData.values[index].toDouble()
                val lat = gribData.latitudes[y].toDouble()
                val lon = gribData.longitudes[x].toDouble()

                // For wind and current, calculate speed first and use that for filtering
                if (type in listOf("wind", "strom")) {
                    val u = gribData.uValues?.get(index)?.toDouble() ?: 0.0
                    val v = gribData.vValues?.get(index)?.toDouble() ?: 0.0
                    val speed = CalculateUtil.calculateSpeedFromUV(u, v)
                    val direction = CalculateUtil.calculateDirectionFromUV(u, v)
                    
                    if (!speed.isNaN() && speed > threshold) {
                        if (lastLat.isNaN() || haversine(lat, lon, lastLat, lastLon) > typeMinDistanceKm) {
                            val color = getColor(type, speed.toDouble())
                            val radius = getRadius(type, speed.toDouble())

                            val properties = """{"type": "$type", "value": $speed, "icon": "$icon", "color": "$color", "radius": $radius, "u": $u, "v": $v, "direction": $direction}"""
                            
                            features.add(
                                """{"type": "Feature", "geometry": {"type": "Point", "coordinates": [$lon, $lat]}, "properties": $properties}"""
                            )
                            lastLat = lat
                            lastLon = lon
                        }
                    }
                } else {
                    // For other types (wave, rain), use the original value for filtering
                    if (!value.isNaN() && value > threshold) {
                        if (lastLat.isNaN() || haversine(lat, lon, lastLat, lastLon) > typeMinDistanceKm) {
                            val color = getColor(type, value)
                            val radius = getRadius(type, value)

                            val properties = """{"type": "$type", "value": $value, "icon": "$icon", "color": "$color", "radius": $radius}"""
                            
                            features.add(
                                """{"type": "Feature", "geometry": {"type": "Point", "coordinates": [$lon, $lat]}, "properties": $properties}"""
                            )
                            lastLat = lat
                            lastLon = lon
                        }
                    }
                }
            }
        }
        return """{"type": "FeatureCollection", "features": [${features.joinToString(",")}] }"""
    }

    fun mergeFeatureCollections(vararg featureCollections: String): String {
        val allFeatures = JSONArray()
        for (fc in featureCollections) {
            try {
                val obj = JSONObject(fc)
                val features = obj.optJSONArray("features")
                if (features != null) {
                    for (i in 0 until features.length()) {
                        allFeatures.put(features.getJSONObject(i))
                    }
                }
            } catch (_: Exception) {
            }
        }
        val merged = JSONObject()
        merged.put("type", "FeatureCollection")
        merged.put("features", allFeatures)
        return merged.toString()
    }
}