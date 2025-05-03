package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.R
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib.GribPoint
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.layers.PropertyFactory.*
import org.json.JSONObject
import org.json.JSONArray
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.sources.GeoJsonOptions

object CurrentOverlay {
    private const val SOURCE_ID = "current-source"
    private const val ARROW_LAYER_ID = "current-arrow-layer"
    private const val ICON_LAYER_ID = "current-icon-layer"
    private const val TEXT_LAYER_ID = "current-text-layer"
    private const val CURRENT_ICON_ID = "current-icon"
    private const val ARROW_ICON_ID = "current-arrow"

    private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap? {
        return try {
            val drawable = AppCompatResources.getDrawable(context, drawableId)
            if (drawable != null) {
                val bitmap = Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            } else {
                Log.e("CurrentOverlay", "Failed to load drawable for id: $drawableId")
                null
            }
        } catch (e: Exception) {
            Log.e("CurrentOverlay", "Error creating bitmap from vector drawable: ${e.message}")
            null
        }
    }

    fun addOrUpdate(context: Context, style: Style, points: List<GribPoint>) {
        // Legg til bilder hvis de ikke allerede er lagt til
        if (style.getImage(CURRENT_ICON_ID) == null) {
            val currentIcon = getBitmapFromVectorDrawable(context, R.drawable.strom)
            if (currentIcon != null) {
                style.addImage(CURRENT_ICON_ID, currentIcon)
            }
        }
        if (style.getImage(ARROW_ICON_ID) == null) {
            val arrowIcon = getBitmapFromVectorDrawable(context, R.drawable.arrow)
            if (arrowIcon != null) {
                style.addImage(ARROW_ICON_ID, arrowIcon)
            }
        }

        // Bygg GeoJSON features
        val features = JSONArray()
        points.forEach { point ->
            val data = point.data ?: return@forEach
            val speed = data.currentSpeed ?: 0f
            val direction = data.currentDirection ?: 0f
            val rotation = GribDirectionUtil.calculateCurrentRotation(direction)
            val roundedSpeed = String.format("%.1f", speed).replace(',', '.').toDouble()

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
                    put("speed", roundedSpeed)
                    put("rotation", rotation.toDouble())
                })
            }
            features.put(feature)
        }

        val geoJson = JSONObject().apply {
            put("type", "FeatureCollection")
            put("features", features)
        }.toString()

        // Legg til/oppdater GeoJSON kilde med clustering
        val source = style.getSource(SOURCE_ID) as? GeoJsonSource
        if (source != null) {
            source.setGeoJson(geoJson)
        } else {
            style.addSource(GeoJsonSource(
                SOURCE_ID,
                geoJson,
                GeoJsonOptions().withCluster(true).withClusterRadius(50)
            ))
        }

        // Legg til/oppdater lag
        if (style.getLayer(ARROW_LAYER_ID) == null) {
            style.addLayer(
                SymbolLayer(ARROW_LAYER_ID, SOURCE_ID)
                    .withProperties(
                        iconImage(ARROW_ICON_ID),
                        iconSize(Expression.interpolate(
                            Expression.linear(), Expression.zoom(),
                            Expression.stop(5, 0.02f),
                            Expression.stop(10, 0.04f),
                            Expression.stop(15, 0.08f)
                        )),
                        iconAllowOverlap(true),
                        iconIgnorePlacement(true),
                        iconRotate(Expression.get("rotation")),
                        iconTranslate(arrayOf(0f, 20f)),
                        iconTranslateAnchor(Property.ICON_TRANSLATE_ANCHOR_VIEWPORT),
                        iconAnchor(Property.ICON_ANCHOR_CENTER),
                        iconOpacity(
                            Expression.step(
                                Expression.zoom(),
                                Expression.literal(0f),
                                Expression.literal(10.0), Expression.literal(1f)
                            )
                        )
                    )
            )
        }

        if (style.getLayer(ICON_LAYER_ID) == null) {
            style.addLayer(
                SymbolLayer(ICON_LAYER_ID, SOURCE_ID)
                    .withFilter(Expression.not(Expression.has("point_count")))
                    .withProperties(
                        iconImage(CURRENT_ICON_ID),
                        iconSize(0.04f),
                        iconAllowOverlap(true),
                        iconIgnorePlacement(true),
                        iconAnchor(Property.ICON_ANCHOR_CENTER)
                    )
            )
        }

        if (style.getLayer(TEXT_LAYER_ID) == null) {
            style.addLayer(
                SymbolLayer(TEXT_LAYER_ID, SOURCE_ID)
                    .withProperties(
                        textField(Expression.concat(
                            Expression.toString(Expression.get("speed")),
                            Expression.literal(" m/s")
                        )),
                        textSize(Expression.interpolate(
                            Expression.linear(), Expression.zoom(),
                            Expression.stop(5, 8f),
                            Expression.stop(10, 12f),
                            Expression.stop(15, 16f)
                        )),
                        textAllowOverlap(true),
                        textIgnorePlacement(true),
                        textOffset(arrayOf(0f, 3.5f)),
                        textAnchor(Property.TEXT_ANCHOR_CENTER),
                        textOpacity(
                            Expression.step(
                                Expression.zoom(),
                                Expression.literal(0f),
                                Expression.literal(7.0), Expression.literal(1f)
                            )
                        )
                    )
            )
        }

        // Legg til cluster-lag for å vise antall punkter i clusteret
        if (style.getLayer("current-cluster-layer") == null) {
            style.addLayer(
                SymbolLayer("current-cluster-layer", SOURCE_ID)
                    .withFilter(Expression.has("point_count"))
                    .withProperties(
                        iconImage(CURRENT_ICON_ID),
                        iconSize(0.06f),
                        iconAllowOverlap(true),
                        iconIgnorePlacement(true),
                        textField(Expression.get("point_count")),
                        textSize(14f),
                        textColor("#000000"),
                        textHaloColor("#ffffff"),
                        textHaloWidth(2.0f),
                        textAnchor(Property.TEXT_ANCHOR_CENTER)
                    )
            )
        }
    }

    fun remove(style: Style) {
        try {
            style.getLayer(TEXT_LAYER_ID)?.let { style.removeLayer(it) }
            style.getLayer(ICON_LAYER_ID)?.let { style.removeLayer(it) }
            style.getLayer(ARROW_LAYER_ID)?.let { style.removeLayer(it) }
            style.getSource(SOURCE_ID)?.let { style.removeSource(it) }
            style.removeImage(CURRENT_ICON_ID)
            style.removeImage(ARROW_ICON_ID)
        } catch (e: Exception) {
            Log.e("CurrentOverlay", "Error removing current overlay: ${e.message}")
        }
    }
} 