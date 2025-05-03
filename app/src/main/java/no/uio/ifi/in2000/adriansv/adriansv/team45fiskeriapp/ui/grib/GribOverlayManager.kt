package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.R
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib.GribData
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib.GribPoint
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.expressions.Expression.*
import org.json.JSONObject
import org.json.JSONArray

object GribOverlayManager {
    private val iconMap = mapOf(
        "wave" to R.drawable.wave,
        "rain" to R.drawable.rain
    )

    // Hjelpefunksjon for å konvertere vector drawable til bitmap
    private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap? {
        val drawable: Drawable = ContextCompat.getDrawable(context, drawableId) ?: return null
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth.takeIf { it > 0 } ?: 64,
            drawable.intrinsicHeight.takeIf { it > 0 } ?: 64,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun addOrUpdateGribOverlay(context: Context, style: Style, geoJson: String) {
        Log.d("GribOverlayManager", "Starting to add/update GRIB overlay")
        Log.d("GribOverlayManager", "GeoJSON: $geoJson")
        
        try {
            val obj = JSONObject(geoJson)
            val features = obj.optJSONArray("features")
            if (features != null) {
                // Grupper punkter etter type
                val windPoints = mutableListOf<GribPoint>()
                val currentPoints = mutableListOf<GribPoint>()
                val otherPoints = mutableListOf<GribPoint>()

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
                        
                        // Beregn retning og hastighet fra u- og v-komponenter
                        val direction = GribDirectionUtil.calculateDirectionFromUV(u.toDouble(), v.toDouble())
                        val speed = GribDirectionUtil.calculateSpeedFromUV(u.toDouble(), v.toDouble())
                        
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
                                Log.d("GribOverlayManager", "Wind point: lat=${point.latitude}, lon=${point.longitude}, speed=$speed, direction=$direction")
                                windPoints.add(point)
                            }
                            "strom" -> {
                                Log.d("GribOverlayManager", "Current point: lat=${point.latitude}, lon=${point.longitude}, speed=$speed, direction=$direction")
                                currentPoints.add(point)
                            }
                            else -> otherPoints.add(point)
                        }
                    }
                }

                // Bruk spesialiserte overlay for vind og strøm
                if (windPoints.isNotEmpty()) {
                    WindOverlay.addOrUpdate(context, style, windPoints)
                }
                if (currentPoints.isNotEmpty()) {
                    CurrentOverlay.addOrUpdate(context, style, currentPoints)
                }

                // Håndter andre typer (bølger og regn) med standard overlay
                if (otherPoints.isNotEmpty()) {
                    addStandardOverlay(context, style, otherPoints)
                }
            }
        } catch (e: Exception) {
            Log.e("GribOverlayManager", "Error processing GeoJSON", e)
        }
    }

    private fun addStandardOverlay(context: Context, style: Style, points: List<GribPoint>) {
        // 1. Legg til ikoner i stilen
        for ((type, resId) in iconMap) {
            try {
                Log.d("GribOverlayManager", "Forsøker å laste ikon for type: $type, resId: $resId")
                val bitmap = getBitmapFromVectorDrawable(context, resId)
                if (bitmap != null) {
                    style.addImage(type, bitmap)
                    Log.d("GribOverlayManager", "La til ikon i stil: $type")
                } else {
                    Log.e("GribOverlayManager", "Bitmap for $type var null!")
                }
            } catch (e: Exception) {
                Log.e("GribOverlayManager", "Kunne ikke laste ikon for $type", e)
            }
        }

        // 2. Bygg GeoJSON for standard overlay
        val features = JSONArray()
        points.forEach { point ->
            val data = point.data ?: return@forEach
            val type = data.variableName
            val value = when (type) {
                "wave" -> data.waveHeight
                "rain" -> data.precipitation
                else -> null
            } ?: return@forEach

            val feature = JSONObject().apply {
                put("type", "Feature")
                put("geometry", JSONObject().apply {
                    put("type", "Point")
                    put("coordinates", JSONArray().apply {
                        put(point.longitude)
                        put(point.latitude)
                    })
                })
                put("properties", JSONObject().apply {
                    put("type", type)
                    put("value", value.toDouble())
                    put("icon", type)
                    put("color", GribOverlayUtil.getColor(type, value.toDouble()))
                    put("radius", GribOverlayUtil.getRadius(type, value.toDouble()))
                })
            }
            features.put(feature)
        }

        val geoJson = JSONObject().apply {
            put("type", "FeatureCollection")
            put("features", features)
        }.toString()

        // 3. Legg til/oppdater GeoJsonSource
        val sourceId = "grib-standard-source"
        if (style.getSource(sourceId) == null) {
            style.addSource(GeoJsonSource(sourceId, geoJson))
        } else {
            (style.getSource(sourceId) as? GeoJsonSource)?.setGeoJson(geoJson)
        }

        // 4. SymbolLayer for ikoner og tall
        val symbolLayerId = "grib-standard-symbol-layer"
        if (style.getLayer(symbolLayerId) == null) {
            val symbolLayer = SymbolLayer(symbolLayerId, sourceId)
                .withProperties(
                    iconImage("{icon}"),
                    iconSize(0.04f),
                    iconAllowOverlap(true),
                    iconIgnorePlacement(true),
                    iconAnchor("center"),
                    textField("{value}"),
                    textSize(14f),
                    textOffset(arrayOf(0f, 2.2f)),
                    textColor("#222222"),
                    textHaloColor("#FFFFFF"),
                    textHaloWidth(1.5f)
                )
            style.addLayer(symbolLayer)
        }

        // 5. CircleLayer for farget tåke
        val circleLayerId = "grib-standard-circle-layer"
        if (style.getLayer(circleLayerId) == null) {
            val circleLayer = CircleLayer(circleLayerId, sourceId)
                .withProperties(
                    circleColor(get("color")),
                    circleRadius(get("radius")),
                    circleOpacity(0.4f)
                )
            style.addLayer(circleLayer)
        }
    }

    fun removeGribOverlay(style: Style) {
        // Fjern standard overlay
        val standardSourceId = "grib-standard-source"
        val standardSymbolLayerId = "grib-standard-symbol-layer"
        val standardCircleLayerId = "grib-standard-circle-layer"
        style.removeLayer(standardSymbolLayerId)
        style.removeLayer(standardCircleLayerId)
        style.removeSource(standardSourceId)

        // Fjern vind og strøm overlay
        WindOverlay.remove(style)
        CurrentOverlay.remove(style)
    }
}