package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.R
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.expressions.Expression.*

object GribOverlayManager {
    private val iconMap = mapOf(
        "wind" to R.drawable.wind,
        "wave" to R.drawable.wave,
        "strom" to R.drawable.strom,
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
        // 2. Logg alle icon-property i geoJson
        try {
            val obj = org.json.JSONObject(geoJson)
            val features = obj.optJSONArray("features")
            if (features != null) {
                for (i in 0 until features.length()) {
                    val feature = features.getJSONObject(i)
                    val props = feature.optJSONObject("properties")
                    val icon = props?.optString("icon")
                    val type = props?.optString("type")
                    Log.d("GribOverlayManager", "Feature $i: type=$type, icon=$icon")
                }
            }
        } catch (e: Exception) {
            Log.e("GribOverlayManager", "Feil ved logging av features i geoJson", e)
        }
        // 3. Legg til/oppdater GeoJsonSource
        val sourceId = "grib-source"
        if (style.getSource(sourceId) == null) {
            style.addSource(GeoJsonSource(sourceId, geoJson))
            Log.d("GribOverlayManager", "La til ny GeoJsonSource for GRIB")
        } else {
            (style.getSource(sourceId) as? GeoJsonSource)?.setGeoJson(geoJson)
            Log.d("GribOverlayManager", "Oppdaterte eksisterende GeoJsonSource for GRIB")
        }
        // 4. SymbolLayer for ikoner og tall
        val symbolLayerId = "grib-symbol-layer"
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
            Log.d("GribOverlayManager", "La til SymbolLayer for GRIB-ikoner")
        }
        // 5. CircleLayer for farget tåke
        val circleLayerId = "grib-circle-layer"
        if (style.getLayer(circleLayerId) == null) {
            val circleLayer = CircleLayer(circleLayerId, sourceId)
                .withProperties(
                    circleColor(get("color")),
                    circleRadius(get("radius")),
                    circleOpacity(0.4f)
                )
            style.addLayer(circleLayer)
            Log.d("GribOverlayManager", "La til CircleLayer for GRIB-tåke")
        }
    }

    fun removeGribOverlay(style: Style) {
        val sourceId = "grib-source"
        val symbolLayerId = "grib-symbol-layer"
        val circleLayerId = "grib-circle-layer"
        style.removeLayer(symbolLayerId)
        style.removeLayer(circleLayerId)
        style.removeSource(sourceId)
    }
}