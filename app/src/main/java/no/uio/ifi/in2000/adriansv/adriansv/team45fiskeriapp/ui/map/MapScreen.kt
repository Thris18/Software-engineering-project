package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.map

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.R
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.grib.GribRepository
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib.GribPoint
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.ship.ShipViewModel
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.ship.ShipInfoCard
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.ship.setupShipLayer
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.ship.updateShipSource
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.farevarsel.GeoJsonViewModel
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.farevarsel.FarevarselPopup
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.theme.Team45FiskeriAppTheme
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
import org.maplibre.android.style.expressions.Expression
import java.io.InputStream
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.ship.Ship
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.farevarsel.AlertInfoCard
import org.maplibre.android.style.layers.Property
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings

private const val TAG = "MapScreen"
private const val SHIP_LAYER_ID = "ship-layer"

@Composable
fun MapScreen(
    viewModel: GeoJsonViewModel = viewModel(),
    shipViewModel: ShipViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val shipUiState by shipViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var mapView: MapView? by remember { mutableStateOf(null) }
    var mapLibreMap: MapLibreMap? by remember { mutableStateOf(null) }
    var selectedPoint: GribPoint? by remember { mutableStateOf(null) }
    var showPopup by remember { mutableStateOf(false) }
    var selectedShip: Ship? by remember { mutableStateOf(null) }
    val gribRepository = remember { GribRepository(context) }
    
    // Filter states
    var showFilterMenu by remember { mutableStateOf(false) }
    var showGrib by remember { mutableStateOf(true) }
    var showAlerts by remember { mutableStateOf(true) }
    var showShips by remember { mutableStateOf(true) }

    // Start ship updates when the screen is created
    LaunchedEffect(Unit) {
        shipViewModel.startPeriodicUpdates()
    }

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
                                    
                                    // Setup ship layer
                                    setupShipLayer(context, style)

                                    val geoJsonData = uiState.geoJsonData
                                    if (geoJsonData != null) {
                                        Log.d(TAG, "Adding GeoJSON source with data length: ${geoJsonData.length}")
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
                                                PropertyFactory.iconOffset(arrayOf(0f, -5f)),
                                                PropertyFactory.symbolPlacement(Property.SYMBOL_PLACEMENT_POINT),
                                                PropertyFactory.iconAnchor(Property.ICON_ANCHOR_CENTER)
                                            )
                                        style.addLayer(symbolLayer)
                                        Log.d(TAG, "Added alert symbol layer")

                                        // Verify that the layer was added
                                        if (style.getLayer("alert-symbol-layer") != null) {
                                            Log.d(TAG, "Alert symbol layer is present in style")
                                        } else {
                                            Log.e(TAG, "Alert symbol layer is missing from style")
                                        }

                                        map.addOnMapClickListener { point ->
                                            val screenPoint = map.projection.toScreenLocation(point)
                                            
                                            // First check for alerts (prioritert)
                                            val alertFeatures = map.queryRenderedFeatures(screenPoint, "alert-symbol-layer")
                                            if (alertFeatures.isNotEmpty()) {
                                                val feature = alertFeatures[0]
                                                val properties = feature.properties()
                                                if (properties != null) {
                                                    // Convert MapLibre JsonObject to org.json.JSONObject
                                                    val jsonObject = JSONObject(properties.toString())
                                                    viewModel.setSelectedAlert(jsonObject)
                                                }
                                                selectedPoint = null
                                                showPopup = false
                                                selectedShip = null
                                                return@addOnMapClickListener true
                                            }
                                            
                                            // Then check for ships
                                            val shipFeatures = map.queryRenderedFeatures(screenPoint, SHIP_LAYER_ID)
                                            if (shipFeatures.isNotEmpty()) {
                                                val feature = shipFeatures[0]
                                                val properties = feature.properties()
                                                
                                                selectedShip = Ship(
                                                    mmsi = properties?.get("mmsi")?.asString ?: "",
                                                    name = properties?.get("name")?.asString ?: "",
                                                    type = properties?.get("type")?.asString ?: "annen_fartoytype",
                                                    displayType = properties?.get("displayType")?.asString ?: "Annet fartøy",
                                                    speed = properties?.get("speed")?.asDouble ?: 0.0,
                                                    course = properties?.get("course")?.asDouble ?: 0.0,
                                                    latitude = point.latitude,
                                                    longitude = point.longitude,
                                                    messageTime = properties?.get("messageTime")?.asString ?: ""
                                                )
                                                selectedPoint = null
                                                showPopup = false
                                                viewModel.setSelectedAlert(null)
                                                return@addOnMapClickListener true
                                            }

                                            // If no alert or ship was clicked, show GRIB data
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
                                            selectedShip = null
                                            false
                                        }
                                    }

                                    // Update layer visibility based on filters
                                    style.getLayer("alert-symbol-layer")?.setProperties(
                                        PropertyFactory.iconOpacity(Expression.literal(if (showAlerts) 1f else 0f))
                                    )
                                    style.getLayer(SHIP_LAYER_ID)?.setProperties(
                                        PropertyFactory.iconOpacity(Expression.literal(if (showShips) 1f else 0f))
                                    )
                                }
                            })
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Settings button
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 24.dp, end = 16.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                tonalElevation = 2.dp
            ) {
                IconButton(
                    onClick = { showFilterMenu = !showFilterMenu },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Innstillinger",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Settings menu
            if (showFilterMenu) {
                SettingsMenu(
                    showGrib = showGrib,
                    showAlerts = showAlerts,
                    showShips = showShips,
                    onGribFilterChanged = { show ->
                        showGrib = show
                        selectedPoint = if (!show) null else selectedPoint
                        showPopup = showGrib && selectedPoint != null
                    },
                    onAlertsFilterChanged = { show ->
                        showAlerts = show
                        mapLibreMap?.getStyle()?.getLayer("alert-symbol-layer")?.setProperties(
                            PropertyFactory.iconOpacity(Expression.literal(if (show) 1f else 0f))
                        )
                        if (!show) viewModel.setSelectedAlert(null)
                    },
                    onShipsFilterChanged = { show ->
                        showShips = show
                        mapLibreMap?.getStyle()?.getLayer(SHIP_LAYER_ID)?.setProperties(
                            PropertyFactory.iconOpacity(Expression.literal(if (show) 1f else 0f))
                        )
                        if (!show) selectedShip = null
                    },
                    onDismiss = { showFilterMenu = false }
                )
            }

            // Update ship positions when they change
            LaunchedEffect(shipUiState.ships) {
                mapLibreMap?.getStyle()?.let { style ->
                    updateShipSource(context, style, shipUiState.ships)
                }
            }

            // Show ship info card if a ship is selected
            selectedShip?.let { ship ->
                ShipInfoCard(
                    ship = ship,
                    onDismiss = { selectedShip = null },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 160.dp)
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

            // Only show GRIB popup if GRIB data is enabled
            if (showGrib && showPopup && selectedPoint != null && uiState.selectedAlert == null) {
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

    var loadedIcons = 0
    var failedIcons = 0

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
                        loadedIcons++
                        Log.d(TAG, "Successfully loaded icon: $iconId")
                    } else {
                        failedIcons++
                        Log.e(TAG, "Failed to decode bitmap for icon: $type-$severity")
                    }
                }
            } catch (e: Exception) {
                failedIcons++
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
                loadedIcons++
                Log.d(TAG, "Successfully loaded icon: extreme")
            } else {
                failedIcons++
                Log.e(TAG, "Failed to decode extreme icon bitmap")
            }
        }
    } catch (e: Exception) {
        failedIcons++
        Log.e(TAG, "Failed to load extreme icon", e)
    }

    Log.d(TAG, "Icon loading summary - Loaded: $loadedIcons, Failed: $failedIcons")
} 