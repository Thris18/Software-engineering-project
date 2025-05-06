package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.R
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib.GribPoint
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.layers.PropertyFactory.*
import org.json.JSONObject
import org.json.JSONArray
import org.maplibre.android.style.expressions.Expression.get
import androidx.appcompat.content.res.AppCompatResources

object RainOverlay {
    private const val SOURCE_ID = "rain-source"
    private const val FOG_LAYER_ID = "rain-fog-layer"
    private const val ICON_LAYER_ID = "rain-icon-layer"
    private const val RAIN_ICON_ID = "rain_icon"
    private const val TEXT_LAYER_ID = "rain-text-layer"
    private const val CLUSTER_LAYER_ID = "rain-cluster-layer"

    private val fogIcons = mapOf(
        "blue" to "rain_fog_blue",
        "yellow" to "rain_fog_yellow",
        "red" to "rain_fog_red"
    )

    private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap? {
        val drawable = AppCompatResources.getDrawable(context, drawableId) ?: return null
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
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

    fun addOrUpdate(context: Context, style: Style, points: List<GribPoint>) {
        addFogImages(context, style)
        val features = JSONArray()
        Log.d("RainOverlay", "Antall punkter til overlay: ${points.size}")
        points.forEach { Log.d("RainOverlay", "Punkt: ${it.latitude}, ${it.longitude}, verdi: ${it.data?.precipitation}") }
        points.forEach { point ->
            val data = point.data ?: return@forEach
            val value = data.precipitation ?: 0f
            val roundedValue = String.format("%.1f", value).replace(',', '.').toDouble()
            val fog = RainOverlayUtil.getFogImageName(value.toDouble())
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
        }
        val geoJson = JSONObject().apply {
            put("type", "FeatureCollection")
            put("features", features)
        }.toString()
        val source = style.getSource(SOURCE_ID) as? org.maplibre.android.style.sources.GeoJsonSource
        if (source != null) {
            source.setGeoJson(geoJson)
        } else {
            style.addSource(
                org.maplibre.android.style.sources.GeoJsonSource(
                    SOURCE_ID,
                    geoJson,
                    org.maplibre.android.style.sources.GeoJsonOptions().withCluster(true).withClusterRadius(50)
                )
            )
        }
        if (style.getLayer(FOG_LAYER_ID) == null) {
            style.addLayer(
                SymbolLayer(FOG_LAYER_ID, SOURCE_ID)
                    .withFilter(org.maplibre.android.style.expressions.Expression.not(org.maplibre.android.style.expressions.Expression.has("point_count")))
                    .withProperties(
                        iconImage(get("fog")),
                        iconSize(1.5f),
                        iconAllowOverlap(true),
                        iconIgnorePlacement(true),
                        iconOpacity(
                            org.maplibre.android.style.expressions.Expression.match(
                                get("fog"),
                                org.maplibre.android.style.expressions.Expression.literal("blue"), org.maplibre.android.style.expressions.Expression.literal(1f),
                                org.maplibre.android.style.expressions.Expression.literal("yellow"), org.maplibre.android.style.expressions.Expression.literal(0.55f),
                                org.maplibre.android.style.expressions.Expression.literal(0.3f) // default (red)
                            )
                        )
                    )
            )
        }
        if (style.getLayer(ICON_LAYER_ID) == null) {
            style.addLayer(
                SymbolLayer(ICON_LAYER_ID, SOURCE_ID)
                    .withFilter(org.maplibre.android.style.expressions.Expression.not(org.maplibre.android.style.expressions.Expression.has("point_count")))
                    .withProperties(
                        iconImage(RAIN_ICON_ID),
                        iconSize(0.5f),
                        iconAllowOverlap(true),
                        iconIgnorePlacement(true)
                    )
            )
        }
        if (style.getLayer(TEXT_LAYER_ID) == null) {
            style.addLayer(
                SymbolLayer(TEXT_LAYER_ID, SOURCE_ID)
                    .withProperties(
                        textField(org.maplibre.android.style.expressions.Expression.concat(
                            org.maplibre.android.style.expressions.Expression.toString(get("value")),
                            org.maplibre.android.style.expressions.Expression.literal(" mm")
                        )),
                        textSize(org.maplibre.android.style.expressions.Expression.interpolate(
                            org.maplibre.android.style.expressions.Expression.linear(), org.maplibre.android.style.expressions.Expression.zoom(),
                            org.maplibre.android.style.expressions.Expression.stop(5, 8f),
                            org.maplibre.android.style.expressions.Expression.stop(10, 12f),
                            org.maplibre.android.style.expressions.Expression.stop(15, 16f)
                        )),
                        textAllowOverlap(true),
                        textIgnorePlacement(true),
                        textOffset(arrayOf(0f, 2f)),
                        textAnchor("center"),
                        textOpacity(
                            org.maplibre.android.style.expressions.Expression.step(
                                org.maplibre.android.style.expressions.Expression.zoom(),
                                org.maplibre.android.style.expressions.Expression.literal(0f),
                                org.maplibre.android.style.expressions.Expression.literal(10.0), org.maplibre.android.style.expressions.Expression.literal(1f)
                            )
                        )
                    )
            )
        }
        if (style.getLayer(CLUSTER_LAYER_ID) == null) {
            style.addLayer(
                SymbolLayer(CLUSTER_LAYER_ID, SOURCE_ID)
                    .withFilter(org.maplibre.android.style.expressions.Expression.has("point_count"))
                    .withProperties(
                        iconImage(RAIN_ICON_ID),
                        iconSize(0.6f),
                        iconAllowOverlap(true),
                        iconIgnorePlacement(true),
                        textField(org.maplibre.android.style.expressions.Expression.get("point_count")),
                        textSize(14f),
                        textColor("#000000"),
                        textHaloColor("#ffffff"),
                        textHaloWidth(2.0f),
                        textAnchor("center")
                    )
            )
        }
    }

    fun remove(style: Style) {
        try {
            style.getLayer(ICON_LAYER_ID)?.let { style.removeLayer(it) }
            style.getLayer(FOG_LAYER_ID)?.let { style.removeLayer(it) }
            style.getSource(SOURCE_ID)?.let { style.removeSource(it) }
            style.removeImage(RAIN_ICON_ID)
            style.removeImage("blue")
            style.removeImage("yellow")
            style.removeImage("red")
        } catch (e: Exception) {
            Log.e("RainOverlay", "Error removing rain overlay: ${e.message}")
        }
    }
} 