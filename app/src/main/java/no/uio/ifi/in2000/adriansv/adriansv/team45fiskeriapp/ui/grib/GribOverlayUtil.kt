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

object GribOverlayUtil {
    // Terskler for de fire typene (økt litt)
    val thresholds = mapOf(
        "wind" to 5.0,
        "wave" to 1.0,
        "strom" to 0.5,
        "rain" to 2.0
    )

    // Farge basert på hvor mye verdien overstiger terskelen
    fun getColor(type: String, value: Double): String {
        val threshold = thresholds[type] ?: return "#888888"
        val ratio = ((value - threshold) / (threshold.takeIf { it > 0 } ?: 1.0)).coerceAtLeast(0.0)
        return when {
            ratio > 1.5 -> "#D32F2F" // Rød
            ratio > 0.5 -> "#F57C00" // Oransje
            ratio > 0.0 -> "#FBC02D" // Gul
            else -> "#66FBC02D" // Lys gul (nesten usynlig)
        }
    }

    // Radius for tåke (i pixels, for MapLibre)
    fun getRadius(type: String, value: Double): Double {
        val threshold = thresholds[type] ?: return 12.0
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
                if (value != null && value > (thresholds[type] ?: Double.MAX_VALUE)) {
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

    fun gribDataToFeatureCollection(
        gribData: GribData,
        type: String,
        icon: String,
        minDistanceKm: Double = 13.0 // Juster radius etter behov
    ): String {
        val features = mutableListOf<String>()
        val threshold = thresholds[type] ?: 0.0
        val width = gribData.width
        val height = gribData.height
        var lastLat = Double.NaN
        var lastLon = Double.NaN
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
                        features.add(
                            """{"type": "Feature", "geometry": {"type": "Point", "coordinates": [$lon, $lat]}, "properties": {"type": "$type", "value": $value, "icon": "$icon", "color": "$color", "radius": $radius}}"""
                        )
                        lastLat = lat
                        lastLon = lon
                    }
                }
            }
        }
        Log.d("GRIB", "Features for $type: ${features.size}")
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