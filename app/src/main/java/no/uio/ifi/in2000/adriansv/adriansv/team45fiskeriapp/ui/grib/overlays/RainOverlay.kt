package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib.overlays

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.appcompat.content.res.AppCompatResources
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.R
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib.GribPoint
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib.utils.RainOverlayUtil
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib.SpatialGrid
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib.utils.GribOverlayUtil
import org.json.JSONArray
import org.json.JSONObject
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonOptions
import org.maplibre.android.style.sources.GeoJsonSource
import java.util.Locale
import androidx.core.graphics.createBitmap

object RainOverlay {
    private const val SOURCE_ID = "rain-source"
    private const val FOG_LAYER_ID = "rain-fog-layer"
    private const val ICON_LAYER_ID = "rain-icon-layer"
    private const val RAIN_ICON_ID = "rain_icon"
    private const val TEXT_LAYER_ID = "rain-text-layer"
    private const val CLUSTER_LAYER_ID = "rain-cluster-layer"


    private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap? {
        val drawable = AppCompatResources.getDrawable(context, drawableId) ?: return null
        val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun addFogImages(context: Context, style: Style) {
        if (style.getImage("blue") == null) {
            val bmp = getBitmapFromVectorDrawable(context, R.drawable.blue)
            if (bmp != null) style.addImage("blue", bmp)
        }
        if (style.getImage("yellow") == null) {
            val bmp = getBitmapFromVectorDrawable(context, R.drawable.yellow)
            if (bmp != null) style.addImage("yellow", bmp)
        }
        if (style.getImage("red") == null) {
            val bmp = getBitmapFromVectorDrawable(context, R.drawable.red)
            if (bmp != null) style.addImage("red", bmp)
        }
        if (style.getImage(RAIN_ICON_ID) == null) {
            val bmp = getBitmapFromVectorDrawable(context, R.drawable.rain)
            if (bmp != null) style.addImage(RAIN_ICON_ID, bmp)
        }
    }

    fun addOrUpdate(context: Context, style: Style, points: List<GribPoint>, spatialGrid: SpatialGrid, isDarkMode: Boolean) {
        addFogImages(context, style)
        val features = JSONArray()
        points.forEach { point ->
            val data = point.data ?: return@forEach
            val value = data.precipitation ?: 0f
            val roundedValue = String.format(Locale.US, "%.2f", value).replace(',', '.').toDouble()
            val threshold = GribOverlayUtil.currentThresholds["rain"] ?: 0.5
            val fog = RainOverlayUtil.getFogImageName(value.toDouble(), threshold)
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
                        put("value", roundedValue)
                        put("fog", fog)
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
        if (style.getLayer(FOG_LAYER_ID) == null) {
            style.addLayer(
                SymbolLayer(FOG_LAYER_ID, SOURCE_ID)
                    .withFilter(Expression.not(Expression.has("point_count")))
                    .withProperties(
                        PropertyFactory.iconImage(Expression.get("fog")),
                        PropertyFactory.iconSize(1.5f),
                        PropertyFactory.iconAllowOverlap(true),
                        PropertyFactory.iconIgnorePlacement(true),
                        PropertyFactory.iconOpacity(
                            Expression.match(
                                Expression.get("fog"),
                                Expression.literal("blue"),
                                Expression.literal(if (isDarkMode) 0.5f else 1f),
                                Expression.literal("yellow"),
                                Expression.literal(if (isDarkMode) 0.25f else 0.55f),
                                Expression.literal(if (isDarkMode) 0.15f else 0.3f)
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
                        PropertyFactory.iconImage(RAIN_ICON_ID),
                        PropertyFactory.iconSize(0.5f),
                        PropertyFactory.iconAllowOverlap(true),
                        PropertyFactory.iconIgnorePlacement(true)
                    )
            )
        }
        if (style.getLayer(TEXT_LAYER_ID) == null) {
            style.addLayer(
                SymbolLayer(TEXT_LAYER_ID, SOURCE_ID)
                    .withProperties(
                        PropertyFactory.textField(
                            Expression.concat(
                                Expression.toString(Expression.get("value")),
                                Expression.literal(" mm")
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
                        PropertyFactory.textOffset(arrayOf(0f, 2f)),
                        PropertyFactory.textAnchor("center"),
                        PropertyFactory.textOpacity(
                            Expression.step(
                                Expression.zoom(),
                                Expression.literal(0f),
                                Expression.literal(10.0), Expression.literal(1f)
                            )
                        ),
                        PropertyFactory.textHaloColor("#ffffff"),
                        PropertyFactory.textHaloWidth(1.0f)
                    )
            )
        }
        if (style.getLayer(CLUSTER_LAYER_ID) == null) {
            style.addLayer(
                SymbolLayer(CLUSTER_LAYER_ID, SOURCE_ID)
                    .withFilter(Expression.has("point_count"))
                    .withProperties(
                        PropertyFactory.iconImage(RAIN_ICON_ID),
                        PropertyFactory.iconSize(0.6f),
                        PropertyFactory.iconAllowOverlap(true),
                        PropertyFactory.iconIgnorePlacement(true),
                        PropertyFactory.textField(Expression.get("point_count")),
                        PropertyFactory.textSize(14f),
                        PropertyFactory.textColor("#000000"),
                        PropertyFactory.textHaloColor("#ffffff"),
                        PropertyFactory.textHaloWidth(2.0f),
                        PropertyFactory.textAnchor("center")
                    )
            )
        }
    }

}