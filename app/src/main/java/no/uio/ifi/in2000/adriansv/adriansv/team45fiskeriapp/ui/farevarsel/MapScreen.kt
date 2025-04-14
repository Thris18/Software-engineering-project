package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.farevarsel

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.grib.GribRepository
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib.GribPoint
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.json.JSONObject
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.theme.Team45FiskeriAppTheme
import org.maplibre.android.style.expressions.Expression
import java.io.InputStream

private const val TAG = "MapScreen"

@Composable
fun MapScreen(
    viewModel: GeoJsonViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var mapView: MapView? by remember { mutableStateOf(null) }
    var mapLibreMap: MapLibreMap? by remember { mutableStateOf(null) }
    var selectedPoint: GribPoint? by remember { mutableStateOf(null) }
    var showPopup by remember { mutableStateOf(false) }
    val gribRepository = remember { GribRepository(context) }

    Team45FiskeriAppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }

            if (uiState.geoJsonData != null) {
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).also { view ->
                            mapView = view
                            view.getMapAsync(OnMapReadyCallback { map ->
                                mapLibreMap = map
                                map.setStyle(Style.Builder().fromUri("https://api.maptiler.com/maps/streets-v2/style.json?key=oMZQoq4zniKOHeMvi7oA")) { style ->
                                    // Set initial camera position to Oslo Fjord
                                    val osloPosition = LatLng(59.9139, 10.7522)
                                    val position = CameraPosition.Builder()
                                        .target(osloPosition)
                                        .zoom(9.0)
                                        .build()
                                    map.moveCamera(CameraUpdateFactory.newCameraPosition(position))

                                    // Load warning icons
                                    loadWarningIcons(context, style)

                                    val geoJsonData = uiState.geoJsonData
                                    if (geoJsonData != null) {
                                        val source = GeoJsonSource("alerts-source", geoJsonData)
                                        style.addSource(source)
                                        Log.d(TAG, "Added GeoJSON source")

                                        val symbolLayer = SymbolLayer("alert-symbol-layer", "alerts-source")
                                            .withProperties(
                                                PropertyFactory.iconImage(
                                                    Expression.match(
                                                        Expression.get("eventAwarenessName"),
                                                        Expression.literal("Sterk ising på skip"), Expression.concat(
                                                            Expression.literal("generic-"),
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
                                                        Expression.literal("generic-yellow")
                                                    )
                                                ),
                                                PropertyFactory.iconSize(0.8f),
                                                PropertyFactory.iconAllowOverlap(true),
                                                PropertyFactory.iconIgnorePlacement(true),
                                                PropertyFactory.iconOffset(arrayOf(0f, -5f))
                                            )
                                        style.addLayer(symbolLayer)

                                        map.addOnMapClickListener { point ->
                                            Log.d(TAG, "Map clicked at: ${point.latitude}, ${point.longitude}")
                                            val screenPoint = map.projection.toScreenLocation(point)
                                            val features = map.queryRenderedFeatures(screenPoint, "alert-symbol-layer")
                                            Log.d(TAG, "Found ${features.size} features at screen point: ${screenPoint.x}, ${screenPoint.y}")

                                            if (features.isNotEmpty()) {
                                                val feature = features[0]
                                                val properties = feature.properties()
                                                Log.d(TAG, "Feature properties: $properties")

                                                val jsonObject = JSONObject().apply {
                                                    put("eventAwarenessName", properties?.get("eventAwarenessName")?.toString() ?: "")
                                                    put("severity", properties?.get("severity")?.toString() ?: "")
                                                    put("area", properties?.get("area")?.toString() ?: "")
                                                    put("description", properties?.get("description")?.toString() ?: "")
                                                    put("recommendation", properties?.get("recommendation")?.toString() ?: "")
                                                }
                                                Log.d(TAG, "Created JSON object: $jsonObject")

                                                // Nullstill GRIB-data og vis farevarsel
                                                selectedPoint = null
                                                showPopup = false
                                                viewModel.setSelectedAlert(jsonObject)
                                                Log.d(TAG, "Set selected alert")
                                                true
                                            } else {
                                                // Hvis ingen farevarsel er funnet og ingen farevarsel er valgt, hent GRIB-data
                                                if (uiState.selectedAlert == null) {
                                                    scope.launch {
                                                        val gribData = gribRepository.getGribData(
                                                            GribPoint(
                                                                latitude = point.latitude,
                                                                longitude = point.longitude
                                                            )
                                                        )
                                                        if (gribData != null) {
                                                            selectedPoint = GribPoint(
                                                                latitude = point.latitude,
                                                                longitude = point.longitude,
                                                                data = gribData
                                                            )
                                                            showPopup = true
                                                        }
                                                    }
                                                }
                                                true
                                            }
                                        }
                                    }
                                }
                            })
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Vis farevarsel-popup hvis et farevarsel er valgt
            uiState.selectedAlert?.let { alert ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    FarevarselPopup(
                        alertData = alert,
                        onDismiss = {
                            viewModel.setSelectedAlert(null)
                        }
                    )
                }
            }

            // Vis GRIB-data popup hvis et punkt er valgt og ingen farevarsel er valgt
            if (showPopup && selectedPoint != null && uiState.selectedAlert == null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    selectedPoint!!.data?.let {
                        AlertInfoCard(
                            alertData = it,
                            onDismiss = {
                                selectedPoint = null
                                showPopup = false
                            }
                        )
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mapView?.onDestroy()
        }
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
                val inputStream: InputStream = context.assets.open(iconPath)
                inputStream.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    if (bitmap != null) {
                        val iconId = "$type-$severity"
                        style.addImage(iconId, bitmap)
                        Log.d(TAG, "Successfully loaded icon: $iconId")
                    } else {
                        Log.e(TAG, "Failed to decode bitmap for icon: $type-$severity")
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Icon not found: $type-$severity") // Some combinations might not exist
            }
        }
    }

    // Load extreme warning icon
    try {
        val inputStream: InputStream = context.assets.open("png/icon-warning-extreme.png")
        inputStream.use { stream ->
            val bitmap = BitmapFactory.decodeStream(stream)
            if (bitmap != null) {
                style.addImage("extreme", bitmap)
                Log.d(TAG, "Successfully loaded icon: extreme")
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to load extreme icon", e)
    }
}