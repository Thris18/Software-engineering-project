package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.farevarsel

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONObject
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import java.lang.ref.WeakReference

private const val TAG = "MapView"

@Composable
fun MapView(
    geoJsonData: String?,
    onAlertSelected: (JSONObject?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    var mapRef by remember { mutableStateOf<WeakReference<MapLibreMap>?>(null) }
    var initialCameraSet by remember { mutableStateOf(false) }

    if (geoJsonData == null) {
        Log.d(TAG, "MapView composable called with null geoJsonData")
    } else {
        Log.d(TAG, "MapView composable called with geoJsonData of length: ${geoJsonData.length}")
    }

    // Håndterer lifecycle events med DisposableEffect
    DisposableEffect(Unit) {
        Log.d(TAG, "Starting MapView lifecycle")
        mapView.onStart()

        onDispose {
            Log.d(TAG, "Stopping MapView lifecycle")
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            if (mapRef == null) {
                Log.d(TAG, "Initializing MapView")
                view.getMapAsync { mapLibreMap ->
                    Log.d(TAG, "Map async loaded")
                    mapRef = WeakReference(mapLibreMap)

                    // Setter posisjonen til Oslofjord
                    setInitialCameraPosition(mapLibreMap)
                    initialCameraSet = true
                    Log.d(TAG, "Initial camera position set")

                    setupMap(mapLibreMap, { clickedFeature ->
                        onAlertSelected(clickedFeature?.getJSONObject("properties"))
                    }, context)
                    Log.d(TAG, "Map setup completed")

                    geoJsonData?.let { data ->
                        if (data.isNotEmpty()) {
                            Log.d(TAG, "Initial data update with geoJsonData length: ${data.length}")
                            updateMapWithGeoJsonData(mapLibreMap, data, initialCameraSet)
                        } else {
                            Log.d(TAG, "GeoJsonData is empty")
                        }
                    } ?: Log.d(TAG, "No initial geoJsonData available")
                }
            } else {
                mapRef?.get()?.let { map ->
                    geoJsonData?.let { data ->
                        if (data.isNotEmpty()) {
                            Log.d(TAG, "Updating existing map with geoJsonData length: ${data.length}")
                            updateMapWithGeoJsonData(map, data, initialCameraSet)
                        } else {
                            Log.d(TAG, "Update called but geoJsonData is empty")
                        }
                    } ?: Log.d(TAG, "Update called but geoJsonData is null")
                } ?: Log.d(TAG, "MapRef weak reference is null or expired")
            }
        }
    )
}

private fun setInitialCameraPosition(map: MapLibreMap) {
    // Oslofjord koordinater
    val osloPosition = LatLng(59.9139, 10.7522)

    val position = CameraPosition.Builder()
        .target(osloPosition)
        .zoom(9.0)
        .build()

    // Flytter kameraet til posisjonen
    map.moveCamera(CameraUpdateFactory.newCameraPosition(position))
}

private fun setupMap(
    map: MapLibreMap,
    onFeatureClick: (JSONObject?) -> Unit,
    context: Context
) {
    map.setStyle("https://api.maptiler.com/maps/streets-v2/style.json?key=oMZQoq4zniKOHeMvi7oA") { style ->
        // Adder kilden
        val geoJsonSource = GeoJsonSource("geojson-source")
        style.addSource(geoJsonSource)

        // Laster ikon
        loadWarningIcons(context, style)

        // Adder bare ikonlag, ikke polygon
        style.addLayer(createAlertSymbolLayer())

        Log.d(TAG, "Map style setup completed - Icons only mode")
    }

    map.addOnMapClickListener { point ->
        val features = map.queryRenderedFeatures(
            map.projection.toScreenLocation(point),
            "alert-symbol-layer"
        )
        Log.d(TAG, "Clicked features: ${features.size}")
        onFeatureClick(features.firstOrNull()?.let { JSONObject(it.toJson()) })
        features.isNotEmpty()
    }
}

private fun loadWarningIcons(context: Context, style: Style) {
    val iconTypes = listOf(
        "wind", "rain", "snow", "lightning", "avalanches", "generic",
        "polarlow", "stormsurge", "flood", "forestfire", "ice",
        "rainflood", "drivingconditions", "landslide"
    )
    val severities = listOf("red", "orange", "yellow")

    for (type in iconTypes) {
        for (severity in severities) {
            try {
                val iconPath = "png/icon-warning-$type-$severity.png"
                Log.d(TAG, "Loading icon from: $iconPath")
                context.assets.open(iconPath).use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    if (bitmap != null) {
                        val iconId = "$type-$severity"
                        style.addImage(iconId, bitmap)
                        Log.d(TAG, "Successfully loaded icon: $iconId")
                    } else {
                        Log.e(TAG, "Failed to decode bitmap for icon: $type-$severity")
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Icon not found: $type-$severity")
            }
        }
    }

    // Laster ekstremvarsel ikon
    try {
        context.assets.open("png/icon-warning-extreme.png").use { inputStream ->
            val bitmap = BitmapFactory.decodeStream(inputStream)
            if (bitmap != null) {
                style.addImage("extreme", bitmap)
                Log.d(TAG, "Successfully loaded icon: extreme")
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to load extreme icon", e)
    }
}

private fun createAlertSymbolLayer() = SymbolLayer("alert-symbol-layer", "geojson-source").apply {
    setProperties(
        PropertyFactory.iconImage(
            Expression.match(
                Expression.get("eventAwarenessName"),
                Expression.literal("Sterk ising på skip"), Expression.concat(
                    Expression.literal("generic-"),  // Using generic warning icon instead of ice
                    Expression.match(
                        Expression.get("severity"),
                        Expression.literal("Severe"), Expression.literal("red"),
                        Expression.literal("Moderate"), Expression.literal("orange"),
                        Expression.literal("Minor"), Expression.literal("yellow"),
                        Expression.literal("yellow")
                    )
                ),
                Expression.literal("Storm"), Expression.concat(
                    Expression.literal("wind-"),
                    Expression.match(
                        Expression.get("severity"),
                        Expression.literal("Severe"), Expression.literal("red"),
                        Expression.literal("Moderate"), Expression.literal("orange"),
                        Expression.literal("Minor"), Expression.literal("yellow"),
                        Expression.literal("yellow")
                    )
                ),
                Expression.literal("Kuling"), Expression.concat(
                    Expression.literal("wind-"),
                    Expression.match(
                        Expression.get("severity"),
                        Expression.literal("Severe"), Expression.literal("red"),
                        Expression.literal("Moderate"), Expression.literal("orange"),
                        Expression.literal("Minor"), Expression.literal("yellow"),
                        Expression.literal("yellow")
                    )
                ),
                Expression.literal("Kraftige vindkast"), Expression.concat(
                    Expression.literal("wind-"),
                    Expression.match(
                        Expression.get("severity"),
                        Expression.literal("Severe"), Expression.literal("red"),
                        Expression.literal("Moderate"), Expression.literal("orange"),
                        Expression.literal("Minor"), Expression.literal("yellow"),
                        Expression.literal("yellow")
                    )
                ),
                Expression.literal("Skogbrannfare"), Expression.concat(
                    Expression.literal("forestfire-"),
                    Expression.match(
                        Expression.get("severity"),
                        Expression.literal("Severe"), Expression.literal("red"),
                        Expression.literal("Moderate"), Expression.literal("orange"),
                        Expression.literal("Minor"), Expression.literal("yellow"),
                        Expression.literal("yellow")
                    )
                ),
                Expression.literal("generic-yellow") // Standard fallback
            )
        ),
        PropertyFactory.iconAllowOverlap(true),
        PropertyFactory.iconIgnorePlacement(true),
        PropertyFactory.iconAnchor(org.maplibre.android.style.layers.Property.ICON_ANCHOR_CENTER),
        PropertyFactory.iconSize(1.0f)
    )
}

private fun updateMapWithGeoJsonData(map: MapLibreMap, data: String, keepCurrentView: Boolean) {
    Log.d(TAG, "Updating map with new GeoJSON data, length: ${data.length}")

    // Håndterer null eller tom data
    if (data.isNullOrBlank() || data == "null") {
        Log.d(TAG, "Empty or null GeoJSON data received")
        map.getStyle { style ->
            val source = style.getSourceAs<GeoJsonSource>("geojson-source")
            source?.setGeoJson("{\"type\":\"FeatureCollection\",\"features\":[]}")
        }
        return
    }

    try {
        val jsonObj = JSONObject(data)
        val features = jsonObj.getJSONArray("features")
        Log.d(TAG, "Number of features: ${features.length()}")

        // Sjekk hvis vi har riktige features å vise
        if (features.length() == 0) {
            Log.d(TAG, "No features found in the GeoJSON data")

            map.getStyle { style ->
                val source = style.getSourceAs<GeoJsonSource>("geojson-source")
                source?.setGeoJson(data)
            }
            return
        }

        for (i in 0 until features.length()) {
            val feature = features.getJSONObject(i)
            val properties = feature.getJSONObject("properties")
            Log.d(TAG, "Feature $i: type=${properties.optString("eventAwarenessName")}, severity=${properties.optString("severity")}")
        }

        map.getStyle { style ->
            val source = style.getSourceAs<GeoJsonSource>("geojson-source")
            source?.setGeoJson(data)
        }

        // Bare zoom til grensene hvis vi ikke ønsker å ha den nåværende viewen
        if (!keepCurrentView) {
            zoomToGeoJsonBounds(map, data)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error parsing GeoJSON: ${e.message}")
        // Prøv å rense kartet hvis dataen er invalid
        try {
            map.getStyle { style ->
                val source = style.getSourceAs<GeoJsonSource>("geojson-source")
                source?.setGeoJson("{\"type\":\"FeatureCollection\",\"features\":[]}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear the map: ${e.message}")
        }
    }
}

private fun zoomToGeoJsonBounds(map: MapLibreMap, geoJsonData: String) {
    val bounds = org.maplibre.android.geometry.LatLngBounds.Builder()
    try {
        val jsonObj = JSONObject(geoJsonData)
        val features = jsonObj.getJSONArray("features")
        var hasCoordinates = false

        for (i in 0 until features.length()) {
            val feature = features.getJSONObject(i)
            val geometry = feature.getJSONObject("geometry")
            if (geometry.getString("type") == "Polygon" || geometry.getString("type") == "MultiPolygon") {
                val coordinates = if (geometry.getString("type") == "Polygon") {
                    geometry.getJSONArray("coordinates").getJSONArray(0)
                } else {
                    geometry.getJSONArray("coordinates").getJSONArray(0).getJSONArray(0)
                }

                for (j in 0 until coordinates.length()) {
                    val coord = coordinates.getJSONArray(j)
                    val latLng = LatLng(coord.getDouble(1), coord.getDouble(0))
                    bounds.include(latLng)
                    hasCoordinates = true
                }
            }
        }

        if (hasCoordinates) {
            map.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error parsing GeoJSON bounds: ${e.message}")
    }
}