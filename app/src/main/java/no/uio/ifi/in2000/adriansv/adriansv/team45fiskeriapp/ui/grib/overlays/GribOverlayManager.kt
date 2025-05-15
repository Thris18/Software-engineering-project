package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib.overlays

import android.content.Context
import android.util.Log
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib.GribData
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib.GribPoint
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.core.utils.CalculateUtil
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib.SpatialGrid
import org.json.JSONObject
import org.maplibre.android.maps.Style

object GribOverlayManager {
    fun addOrUpdateGribOverlay(context: Context, style: Style, geoJson: String, isDarkMode: Boolean) {

        try {
            val obj = JSONObject(geoJson)
            val features = obj.optJSONArray("features")
            if (features != null) {
                // group points by type
                val windPoints = mutableListOf<GribPoint>()
                val currentPoints = mutableListOf<GribPoint>()
                val wavePoints = mutableListOf<GribPoint>()
                val rainPoints = mutableListOf<GribPoint>()

                for (i in 0 until features.length()) {
                    val feature = features.getJSONObject(i)
                    val props = feature.optJSONObject("properties")
                    val type = props?.optString("type")
                    val value = props?.optDouble("value")?.toFloat()
                    val geometry = feature.optJSONObject("geometry")
                    val coordinates = geometry?.optJSONArray("coordinates")

                    if (coordinates != null && coordinates.length() >= 2) {
                        val u = props?.optDouble("u")?.toFloat() ?: 0f
                        val v = props?.optDouble("v")?.toFloat() ?: 0f

                        // calculate direction and speed from u and v
                        val direction = CalculateUtil.calculateDirectionFromUV(u.toDouble(), v.toDouble())
                        val speed = CalculateUtil.calculateSpeedFromUV(u.toDouble(), v.toDouble())

                        val point = GribPoint(
                            latitude = coordinates.getDouble(1),
                            longitude = coordinates.getDouble(0),
                            data = GribData(
                                values = floatArrayOf(speed),
                                width = 1,
                                height = 1,
                                latitudes = floatArrayOf(coordinates.getDouble(1).toFloat()),
                                longitudes = floatArrayOf(coordinates.getDouble(0).toFloat()),
                                minValue = speed,
                                maxValue = speed,
                                variableName = type ?: "",
                                unit = "",
                                referenceTime = "",
                                windSpeed = if (type == "wind") speed else null,
                                windDirection = if (type == "wind") direction else null,
                                currentSpeed = if (type == "strom") speed else null,
                                currentDirection = if (type == "strom") direction else null,
                                waveHeight = if (type == "wave") value else null,
                                precipitation = if (type == "rain") value else null
                            )
                        )

                        when (type) {
                            "wind" -> {
                                windPoints.add(point)
                            }
                            "strom" -> {
                                currentPoints.add(point)
                            }
                            "wave" -> wavePoints.add(point)
                            "rain" -> rainPoints.add(point)
                        }
                    }
                }

                val minDistanceKm = 6.5
                val spatialGrid = SpatialGrid(minDistanceKm)

                if (windPoints.isNotEmpty()) {
                    WindOverlay.addOrUpdate(context, style, windPoints, spatialGrid, isDarkMode)
                }
                if (currentPoints.isNotEmpty()) {
                    CurrentOverlay.addOrUpdate(context, style, currentPoints, spatialGrid, isDarkMode)
                }
                if (wavePoints.isNotEmpty()) {
                    WaveOverlay.addOrUpdate(context, style, wavePoints, spatialGrid, isDarkMode)
                }
                if (rainPoints.isNotEmpty()) {
                    RainOverlay.addOrUpdate(context, style, rainPoints, spatialGrid, isDarkMode)
                }
            }
        } catch (e: Exception) {
            Log.e("GribOverlayManager", "Error processing GeoJSON", e)
        }
    }

}