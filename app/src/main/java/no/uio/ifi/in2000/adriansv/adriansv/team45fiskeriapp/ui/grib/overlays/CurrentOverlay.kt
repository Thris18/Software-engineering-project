package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib.overlays

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.appcompat.content.res.AppCompatResources
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.R
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib.GribPoint
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.core.utils.CalculateUtil
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib.SpatialGrid
import org.json.JSONArray
import org.json.JSONObject
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonOptions
import org.maplibre.android.style.sources.GeoJsonSource
import java.util.Locale
import androidx.core.graphics.createBitmap

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
                val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun addOrUpdate(context: Context, style: Style, points: List<GribPoint>, spatialGrid: SpatialGrid, isDarkMode: Boolean) {
        if (style.getImage(CURRENT_ICON_ID) == null) {
            val currentIcon = getBitmapFromVectorDrawable(context, if (isDarkMode) R.drawable.dark_strom else R.drawable.strom)
            if (currentIcon != null) {
                style.addImage(CURRENT_ICON_ID, currentIcon)
            }
        }
        if (style.getImage(ARROW_ICON_ID) == null) {
            val arrowIcon = getBitmapFromVectorDrawable(context, if (isDarkMode) R.drawable.dark_arrow else R.drawable.arrow)
            if (arrowIcon != null) {
                style.addImage(ARROW_ICON_ID, arrowIcon)
            }
        }

        // Build GeoJSON from points
        val features = JSONArray()
        points.forEach { point ->
            val data = point.data ?: return@forEach
            val speed = data.currentSpeed ?: 0f
            val direction = data.currentDirection ?: 0f
            val rotation = CalculateUtil.calculateCurrentRotation(direction)
            val roundedSpeed = String.format(Locale.US, "%.2f", speed).replace(',', '.').toDouble()
            if (spatialGrid.isFarFromAll(point.latitude, point.longitude)) {
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
                spatialGrid.addPoint(point.latitude, point.longitude)
            }

        }

        val geoJson = JSONObject().apply {
            put("type", "FeatureCollection")
            put("features", features)
        }.toString()

        //  Add GeoJSON source
        val source = style.getSource(SOURCE_ID) as? GeoJsonSource
        if (source != null) {
            source.setGeoJson(geoJson)
        } else {
            style.addSource(
                GeoJsonSource(
                    SOURCE_ID,
                    geoJson,
                    GeoJsonOptions().withCluster(true).withClusterRadius(50)
                )
            )
        }

        // Add layers
        if (style.getLayer(ARROW_LAYER_ID) == null) {
            style.addLayer(
                SymbolLayer(ARROW_LAYER_ID, SOURCE_ID)
                    .withProperties(
                        PropertyFactory.iconImage(ARROW_ICON_ID),
                        PropertyFactory.iconSize(
                            Expression.interpolate(
                                Expression.linear(), Expression.zoom(),
                                Expression.stop(5, 0.02f),
                                Expression.stop(10, 0.04f),
                                Expression.stop(15, 0.08f)
                            )
                        ),
                        PropertyFactory.iconAllowOverlap(true),
                        PropertyFactory.iconIgnorePlacement(true),
                        PropertyFactory.iconRotate(Expression.get("rotation")),
                        PropertyFactory.iconTranslate(arrayOf(0f, 20f)),
                        PropertyFactory.iconTranslateAnchor(Property.ICON_TRANSLATE_ANCHOR_VIEWPORT),
                        PropertyFactory.iconAnchor(Property.ICON_ANCHOR_CENTER),
                        PropertyFactory.iconOpacity(
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
                        PropertyFactory.iconImage(CURRENT_ICON_ID),
                        PropertyFactory.iconSize(0.5f),
                        PropertyFactory.iconAllowOverlap(true),
                        PropertyFactory.iconIgnorePlacement(true),
                        PropertyFactory.iconAnchor(Property.ICON_ANCHOR_CENTER)
                    )
            )
        }

        if (style.getLayer(TEXT_LAYER_ID) == null) {
            style.addLayer(
                SymbolLayer(TEXT_LAYER_ID, SOURCE_ID)
                    .withProperties(
                        PropertyFactory.textField(
                            Expression.concat(
                                Expression.toString(Expression.get("speed")),
                                Expression.literal(" m/s")
                            )
                        ),
                        PropertyFactory.textSize(
                            Expression.interpolate(
                                Expression.linear(), Expression.zoom(),
                                Expression.stop(5, 8f),
                                Expression.stop(10, 12f),
                                Expression.stop(15, 16f)
                            )
                        ),
                        PropertyFactory.textAllowOverlap(true),
                        PropertyFactory.textIgnorePlacement(true),
                        PropertyFactory.textOffset(arrayOf(0f, 3.5f)),
                        PropertyFactory.textAnchor(Property.TEXT_ANCHOR_CENTER),
                        PropertyFactory.textOpacity(
                            Expression.step(
                                Expression.zoom(),
                                Expression.literal(0f),
                                Expression.literal(7.0), Expression.literal(1f)
                            )
                        ),
                        PropertyFactory.textHaloColor("#ffffff"),
                        PropertyFactory.textHaloWidth(1.0f)
                    )
            )
        }

        // Add cluster layers
        if (style.getLayer("current-cluster-layer") == null) {
            style.addLayer(
                SymbolLayer("current-cluster-layer", SOURCE_ID)
                    .withFilter(Expression.has("point_count"))
                    .withProperties(
                        PropertyFactory.iconImage(CURRENT_ICON_ID),
                        PropertyFactory.iconSize(0.6f),
                        PropertyFactory.iconAllowOverlap(true),
                        PropertyFactory.iconIgnorePlacement(true),
                        PropertyFactory.textField(Expression.get("point_count")),
                        PropertyFactory.textSize(14f),
                        PropertyFactory.textColor("#000000"),
                        PropertyFactory.textHaloColor("#ffffff"),
                        PropertyFactory.textHaloWidth(2.0f),
                        PropertyFactory.textAnchor(Property.TEXT_ANCHOR_CENTER)
                    )
            )
        }

    }

}