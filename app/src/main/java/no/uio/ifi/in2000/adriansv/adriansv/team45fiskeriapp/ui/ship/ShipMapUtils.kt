package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.ship

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import androidx.core.content.ContextCompat
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.R
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.ship.Ship
import org.json.JSONArray
import org.json.JSONObject
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import androidx.core.graphics.createBitmap

private const val TAG = "ShipMapUtils"
private const val SHIP_SOURCE_ID = "ship-source"
private const val SHIP_LAYER_ID = "ship-layer"

// Map av skipstyper til drawable ressurser
private val shipTypeToDrawable = mapOf(
    "fiskefartoy" to R.drawable.fiskefartoy,
    "taubat" to R.drawable.taubat,
    "dykkerfartoy" to R.drawable.dykkerfartoy,
    "mindre_arbeidsbat" to R.drawable.mindre_arbeidsbat,
    "hoyhastighetsfartoy" to R.drawable.hoyhastighetsfartoy,
    "losbat" to R.drawable.losbat,
    "sar" to R.drawable.sar,
    "politi" to R.drawable.politi,
    "passasjerfartoy" to R.drawable.passasjerfartoy,
    "fraktefartoy" to R.drawable.fraktefartoy,
    "reservert" to R.drawable.reservert,
    "annen_fartoytype" to R.drawable.annen_fartoytype
)

private fun vectorToBitmap(context: Context, drawableId: Int): Bitmap? {
    return try {
        val drawable = ContextCompat.getDrawable(context, drawableId)
        if (drawable == null) {
            Log.e(TAG, "Failed to get drawable for id: $drawableId")
            return null
        }
        
        val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap
    } catch (e: Exception) {
        Log.e(TAG, "Error converting vector to bitmap", e)
        null
    }
}

fun setupShipLayer(context: Context, style: Style) {
    Log.d(TAG, "Setting up ship layer...")
    
    // Remove existing layer and source if they exist
    style.removeLayer(SHIP_LAYER_ID)
    style.removeSource(SHIP_SOURCE_ID)
    
    // Add ship icons for each type
    shipTypeToDrawable.forEach { (type, drawableId) ->
        vectorToBitmap(context, drawableId)?.let { bitmap ->
            style.addImage(type, bitmap)
            Log.d(TAG, "Added ship icon for type: $type")
        }
    }

    val emptyFeatureCollection = JSONObject().apply {
        put("type", "FeatureCollection")
        put("features", JSONArray())
    }
    val source = GeoJsonSource(SHIP_SOURCE_ID, emptyFeatureCollection.toString())
    style.addSource(source)
    Log.d(TAG, "Added ship source")

    val symbolLayer = SymbolLayer(SHIP_LAYER_ID, SHIP_SOURCE_ID)
        .withProperties(
            PropertyFactory.iconImage(Expression.get("type")),
            PropertyFactory.iconAllowOverlap(true),
            PropertyFactory.iconIgnorePlacement(true),
            PropertyFactory.iconRotate(Expression.get("course")),
            PropertyFactory.iconSize(
                Expression.interpolate(
                    Expression.linear(),
                    Expression.zoom(),
                    Expression.literal(10), Expression.literal(0.6),
                    Expression.literal(16), Expression.literal(1.2)
                )
            )
        )
    style.addLayer(symbolLayer)
}

fun updateShipSource(context: Context, style: Style, ships: List<Ship>) {
    
    try {
        val features = JSONArray()
        ships.forEach { ship ->
            val feature = JSONObject().apply {
                put("type", "Feature")
                put("geometry", JSONObject().apply {
                    put("type", "Point")
                    put("coordinates", JSONArray().apply {
                        put(ship.longitude)
                        put(ship.latitude)
                    })
                })
                put("properties", JSONObject().apply {
                    put("id", ship.mmsi)
                    put("course", ship.course)
                    put("speed", ship.speed)
                    put("mmsi", ship.mmsi)
                    put("name", ship.name)
                    put("messageTime", ship.messageTime)
                    put("type", ship.type)
                    put("displayType", ship.displayType)
                })
            }
            features.put(feature)
        }
        
        val featureCollection = JSONObject().apply {
            put("type", "FeatureCollection")
            put("features", features)
        }
        
        val source = style.getSource(SHIP_SOURCE_ID) as? GeoJsonSource
        if (source != null) {
            source.setGeoJson(featureCollection.toString())
            Log.d(TAG, "Updated ship source with GeoJSON")
        } else {
            Log.e(TAG, "Ship source not found, recreating...")
            val newSource = GeoJsonSource(SHIP_SOURCE_ID, featureCollection.toString())
            style.addSource(newSource)
            setupShipLayer(context, style)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error updating ship source", e)
    }
} 