package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib

import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib.GribPoint
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib.GribData
import android.graphics.Color
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import android.content.Context
import android.content.SharedPreferences

object GribOverlayUtil {
    private const val PREFS_NAME = "GribThresholds"
    private const val KEY_WIND = "wind_threshold"
    private const val KEY_WAVE = "wave_threshold"
    private const val KEY_CURRENT = "current_threshold"
    private const val KEY_RAIN = "rain_threshold"

    // Standard terskelverdier som fallback
    private val defaultThresholds = mapOf(
        "wind" to 2.0,
        "wave" to 0.3,
        "strom" to 0.1,
        "rain" to 0.5
    )

    // Dynamiske terskelverdier som kan oppdateres
    private var currentThresholds = defaultThresholds.toMutableMap()

    // Initialiser med lagrede verdier
    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        currentThresholds["wind"] = prefs.getFloat(KEY_WIND, defaultThresholds["wind"]!!.toFloat()).toDouble()
        currentThresholds["wave"] = prefs.getFloat(KEY_WAVE, defaultThresholds["wave"]!!.toFloat()).toDouble()
        currentThresholds["strom"] = prefs.getFloat(KEY_CURRENT, defaultThresholds["strom"]!!.toFloat()).toDouble()
        currentThresholds["rain"] = prefs.getFloat(KEY_RAIN, defaultThresholds["rain"]!!.toFloat()).toDouble()
    }

    // Funksjon for å oppdatere terskelverdier
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

        // Lagre verdiene
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
            putFloat(KEY_WIND, windThreshold)
            putFloat(KEY_WAVE, waveThreshold)
            putFloat(KEY_CURRENT, currentThreshold)
            putFloat(KEY_RAIN, precipitationThreshold)
            apply()
        }
    }

    // Farge basert på hvor mye verdien overstiger terskelen
    fun getColor(type: String, value: Double): Int {
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

    // Radius for tåke (i pixels, for MapLibre)
    fun getRadius(type: String, value: Double): Double {
        val threshold = currentThresholds[type] ?: defaultThresholds[type] ?: return 12.0
        val base = 12.0
        val extra = ((value - threshold) * 2.0).coerceAtLeast(0.0)
        return base + extra
    }

    // Lag GeoJSON FeatureCollection for alle fire typene
    fun gribPointToFeatureCollection(gribPoint: GribPoint): String {
        val features = mutableListOf<String>()
        val lat = gribPoint.latitude
        val lon = gribPoint.longitude
        val data = gribPoint.data
        if (data != null) {
            val gribTypes = listOf(
                Triple("wind", data.windSpeed, "wind"),
                Triple("wave", data.waveHeight, "wave"),
                Triple("strom", data.currentSpeed, "strom"),
                Triple("rain", data.precipitation, "rain")
            )
            for ((type, value, icon) in gribTypes) {
                if (value != null && value > (currentThresholds[type] ?: Double.MAX_VALUE)) {
                    val color = getColor(type, value.toDouble())
                    val radius = getRadius(type, value.toDouble())
                    features.add("""
                        {"type": "Feature", "geometry": {"type": "Point", "coordinates": [$lon, $lat]}, "properties": {"type": "$type", "value": $value, "icon": "$icon", "color": "$color", "radius": $radius}}
                    """.trimIndent())
                }
            }
        }
        return """{"type": "FeatureCollection", "features": [${features.joinToString(",")}]}"""
    }

    // Hjelpefunksjon for å regne ut avstand mellom to lat/lon-punkter (i km)
    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0 // Radius of Earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }

    // Hjelpefunksjon for å beregne retning fra u og v komponenter
    private fun calculateDirection(u: Double, v: Double): Double {
        // Beregn retning i grader (0-360)
        val direction = Math.toDegrees(atan2(v, u))
        // Konverter til meteorologisk retning (hvor vinden kommer fra)
        return (direction + 180) % 360
    }

    fun gribDataToFeatureCollection(
        gribData: GribData,
        type: String,
        icon: String,
        minDistanceKm: Double = 10.0 // Juster radius etter behov
    ): String {
        val features = mutableListOf<String>()
        val threshold = currentThresholds[type] ?: 0.0
        val width = gribData.width
        val height = gribData.height
        var lastLat = Double.NaN
        var lastLon = Double.NaN

        // Hent u- og v-komponenter basert på type
        val uComponent = when (type) {
            "wind" -> "u-component_of_wind_height_above_ground"
            "strom" -> "u-component_of_current_depth_below_sea"
            else -> null
        }
        val vComponent = when (type) {
            "wind" -> "v-component_of_wind_height_above_ground"
            "strom" -> "v-component_of_current_depth_below_sea"
            else -> null
        }

        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val value = gribData.values[index].toDouble()
                val lat = gribData.latitudes[y].toDouble()
                val lon = gribData.longitudes[x].toDouble()

                if (!value.isNaN() && value > threshold) {
                    // Sjekk avstand til forrige ikon av samme type
                    if (lastLat.isNaN() || haversine(lat, lon, lastLat, lastLon) > minDistanceKm) {
                        val color = getColor(type, value)
                        val radius = getRadius(type, value)

                        // For vind og strøm, inkluder u- og v-komponenter
                        val properties = if (type in listOf("wind", "strom")) {
                            val u = gribData.uValues?.get(index)?.toDouble() ?: 0.0
                            val v = gribData.vValues?.get(index)?.toDouble() ?: 0.0
                            val speed = GribDirectionUtil.calculateSpeedFromUV(u, v)
                            val direction = GribDirectionUtil.calculateDirectionFromUV(u, v)
                            """{"type": "$type", "value": $speed, "icon": "$icon", "color": "$color", "radius": $radius, "u": $u, "v": $v, "direction": $direction}"""
                        } else {
                            """{"type": "$type", "value": $value, "icon": "$icon", "color": "$color", "radius": $radius}"""
                        }

                        features.add(
                            """{"type": "Feature", "geometry": {"type": "Point", "coordinates": [$lon, $lat]}, "properties": $properties}"""
                        )
                        lastLat = lat
                        lastLon = lon
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
            } catch (e: Exception) {
                // Ignorer ugyldig JSON
            }
        }
        val merged = JSONObject()
        merged.put("type", "FeatureCollection")
        merged.put("features", allFeatures)
        return merged.toString()
    }
} 