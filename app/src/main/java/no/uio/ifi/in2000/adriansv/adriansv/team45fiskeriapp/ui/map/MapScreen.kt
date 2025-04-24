package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.map

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
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
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.grib.GribRepository
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.weather.WeatherDataSource
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.weather.WeatherRepository
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib.GribPoint
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.ship.Ship
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components.BaatvettButton
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components.BaatvettOverlay
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components.ProfilePopup
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components.SettingsPopup
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.farevarsel.AlertInfoCard
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.farevarsel.FarevarselPopup
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.farevarsel.GeoJsonViewModel
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.ship.ShipInfoCard
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.ship.ShipViewModel
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.ship.setupShipLayer
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.ship.updateShipSource
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.theme.Team45FiskeriAppTheme
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.weather.WeatherInfoBox
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.weather.WeatherViewModel
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.weather.WeatherViewModelFactory
import org.json.JSONObject
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import java.io.InputStream

private const val TAG = "MapScreen"
private const val SHIP_LAYER_ID = "ship-layer"

private fun updateAlertPolygon(style: Style, alertId: String?) {
    val layer = style.getLayer("alert-polygon-layer") as? FillLayer ?: return

    if (alertId != null) {
        // Vis kun polygoner med samme id
        layer.setFilter(
            Expression.all(
                Expression.any(
                    Expression.eq(Expression.geometryType(), Expression.literal("Polygon")),
                    Expression.eq(Expression.geometryType(), Expression.literal("MultiPolygon"))
                ),
                Expression.eq(Expression.get("id"), Expression.literal(alertId))
            )
        )
        layer.setProperties(PropertyFactory.fillOpacity(0.5f))
    } else {
        // Gjem alt
        layer.setProperties(PropertyFactory.fillOpacity(0f))
    }
}

@Composable
fun MapScreen(
    onNavigateToProfile: () -> Unit,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    isDarkMode: Boolean,
    showGrib: Boolean,
    showAlerts: Boolean,
    showShips: Boolean,
    onGribFilterChanged: (Boolean) -> Unit,
    onAlertsFilterChanged: (Boolean) -> Unit,
    onShipsFilterChanged: (Boolean) -> Unit
) {
    val weatherDataSource = WeatherDataSource()
    val weatherRepository = WeatherRepository(weatherDataSource)
    val weatherViewModel: WeatherViewModel = viewModel(
        factory = WeatherViewModelFactory(weatherRepository)
    )
    val viewModel: GeoJsonViewModel = viewModel()
    val shipViewModel: ShipViewModel = viewModel()
    
    // Collect states
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val shipUiState by shipViewModel.uiState.collectAsStateWithLifecycle()
    val weatherUiState by weatherViewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var mapView: MapView? by remember { mutableStateOf(null) }
    var mapLibreMap: MapLibreMap? by remember { mutableStateOf(null) }
    var selectedPoint: GribPoint? by remember { mutableStateOf(null) }
    var showPopup by remember { mutableStateOf(false) }
    var selectedShip: Ship? by remember { mutableStateOf(null) }
    var selectedShipScreenPosition by remember { mutableStateOf<android.graphics.PointF?>(null) }
    val gribRepository = remember { GribRepository(context) }
    
    // Filter states
    var showBaatvettRules by remember { mutableStateOf(false) }
    var showSettingsPopup by remember { mutableStateOf(false) }
    var showProfilePopup by remember { mutableStateOf(false) }
    
    // Brukerinformasjon states
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    
    // Sett opp dark mode
    val isDarkTheme = isDarkMode

    // Helper function to reset selection states
    fun resetSelections() {
        selectedPoint = null
        showPopup = false
        viewModel.setSelectedAlert(null)
    }

    // Start ship updates when the screen is created
    LaunchedEffect(Unit) {
        shipViewModel.startPeriodicUpdates()
    }

    Team45FiskeriAppTheme(darkTheme = isDarkTheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            uiState.error?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }

            if (uiState.geoJsonData != null) {
                // Map view
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

                                    // Add camera movement listener for weather updates
                                    map.addOnCameraIdleListener {
                                        val center = map.cameraPosition.target
                                        val zoom = map.cameraPosition.zoom
                                        if (center != null) {
                                            weatherViewModel.updateWeather(
                                                latitude = center.latitude,
                                                longitude = center.longitude,
                                                zoomLevel = zoom
                                            )
                                        }
                                    }

                                    // Initial weather update
                                    weatherViewModel.updateWeather(
                                        latitude = osloPosition.latitude,
                                        longitude = osloPosition.longitude,
                                        zoomLevel = position.zoom
                                    )

                                    // Load warning icons and setup layers
                                    loadWarningIcons(context, style)
                                    setupShipLayer(context, style)

                                    // Add GeoJSON source and layer for alerts
                                    val source = GeoJsonSource("alerts-source", uiState.geoJsonData)
                                    style.addSource(source)

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

                                    // Legg til polygon-laget under symbol-laget
                                    val polygonLayer = FillLayer("alert-polygon-layer", "alerts-source")
                                        .withProperties(
                                            PropertyFactory.fillColor(
                                                Expression.match(
                                                    Expression.get("severity"),
                                                    Expression.literal("Severe"), Expression.color(Color.parseColor("#66D32F2F")),
                                                    Expression.literal("Moderate"), Expression.color(Color.parseColor("#66F57C00")),
                                                    Expression.literal("Minor"), Expression.color(Color.parseColor("#66FBC02D")),
                                                    Expression.color(Color.parseColor("#66FBC02D"))
                                                )
                                            ),
                                            PropertyFactory.fillOpacity(0f),
                                            PropertyFactory.fillOutlineColor(Color.parseColor("#000000"))
                                        )
                                        .withFilter(
                                            Expression.any(
                                                Expression.eq(Expression.geometryType(), Expression.literal("Polygon")),
                                                Expression.eq(Expression.geometryType(), Expression.literal("MultiPolygon"))
                                            )
                                        )

                                    style.addLayerBelow(polygonLayer, "alert-symbol-layer")

                                    map.addOnMapClickListener { point ->
                                        val screenPoint = map.projection.toScreenLocation(point)
                                        
                                        // Check for alerts (prioritert) - kun hvis alerts er aktivert
                                        if (showAlerts) {
                                            val alertFeatures = map.queryRenderedFeatures(screenPoint, "alert-symbol-layer")
                                            if (alertFeatures.isNotEmpty()) {
                                                val feature = alertFeatures[0]
                                                val properties = feature.properties()
                                                if (properties != null) {
                                                    val jsonObject = JSONObject(properties.toString())
                                                    val id = properties.get("id").asString
                                                    resetSelections()
                                                    viewModel.setSelectedAlert(jsonObject)
                                                    
                                                    // Vis polygon for dette varselet
                                                    map.getStyle { style ->
                                                        updateAlertPolygon(style, id)
                                                    }
                                                }
                                                return@addOnMapClickListener true
                                            }
                                        }
                                        
                                        // Check for ships - kun hvis ships er aktivert
                                        if (showShips) {
                                            val shipFeatures = map.queryRenderedFeatures(screenPoint, SHIP_LAYER_ID)
                                            if (shipFeatures.isNotEmpty()) {
                                                resetSelections()  // Nullstill alerts og grib-state først
                                                val feature = shipFeatures[0]
                                                val properties = feature.properties()
                                                
                                                if (properties != null) {
                                                    val mmsi = properties.get("mmsi")?.asString
                                                    if (mmsi != null) {
                                                        selectedShip = shipUiState.ships.find { it.mmsi == mmsi }
                                                        if (selectedShip != null) {
                                                            selectedShipScreenPosition = mapLibreMap?.projection?.toScreenLocation(
                                                                LatLng(selectedShip!!.latitude, selectedShip!!.longitude)
                                                            )
                                                        }
                                                    }
                                                }
                                                
                                                // Gjem polygon når skip velges
                                                map.getStyle { style ->
                                                    updateAlertPolygon(style, null)
                                                }
                                                return@addOnMapClickListener true
                                            }
                                        }
                                        
                                        // If no alert or ship was clicked, show GRIB data if enabled
                                        if (showGrib && uiState.selectedAlert == null) {
                                            scope.launch {
                                                val gribData = gribRepository.getGribData(
                                                    GribPoint(
                                                        latitude = point.latitude,
                                                        longitude = point.longitude
                                                    )
                                                )
                                                if (gribData != null) {
                                                    resetSelections()
                                                    selectedPoint = GribPoint(
                                                        latitude = point.latitude,
                                                        longitude = point.longitude,
                                                        data = gribData
                                                    )
                                                    showPopup = true
                                                    
                                                    // Gjem polygon når GRIB data vises
                                                    map.getStyle { style ->
                                                        updateAlertPolygon(style, null)
                                                    }
                                                }
                                            }
                                        }
                                        
                                        false
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

            // UI Elements
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.weight(1f))

                // Båtvett button
                BaatvettButton(
                    onClick = { showBaatvettRules = true },
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .align(Alignment.End)
                )
            }

            // Weather info box - now directly in the main Box, vertically centered
            if (weatherUiState.weather != null) {
                WeatherInfoBox(
                    weather = weatherUiState.weather!!,
                    weatherState = weatherUiState,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                )
            }
            
            // Popups and overlays
            if (showBaatvettRules) {
                BaatvettOverlay(
                    onDismiss = { showBaatvettRules = false }
                )
            }

            if (showSettingsPopup) {
                SettingsPopup(
                    isDarkMode = isDarkMode,
                    showGrib = showGrib,
                    showAlerts = showAlerts,
                    showShips = showShips,
                    onDarkModeChange = { newDarkMode ->
                        // Handle dark mode change
                        onNavigate("kart") // Refresh the screen with new theme
                    },
                    onGribFilterChanged = onGribFilterChanged,
                    onAlertsFilterChanged = onAlertsFilterChanged,
                    onShipsFilterChanged = onShipsFilterChanged,
                    onDismiss = { showSettingsPopup = false }
                )
            }

            if (showProfilePopup) {
                ProfilePopup(
                    userName = "$firstName $lastName",
                    onUserNameChange = { /* Handle name change */ },
                    onSettingsClick = { 
                        showSettingsPopup = true
                        showProfilePopup = false
                    },
                    onYourInformationClick = {
                        // Handle your information click
                    },
                    onDismiss = { showProfilePopup = false }
                )
            }

            // Show ship info card if a ship is selected
            selectedShip?.let { ship ->
                ShipInfoCard(
                    ship = ship,
                    onDismiss = {
                        selectedShip = null
                        selectedShipScreenPosition = null
                    },
                    shipScreenPosition = selectedShipScreenPosition
                )
            }

            // Vis farevarsel-popup hvis et farevarsel er valgt
            uiState.selectedAlert?.let { alert ->
                FarevarselPopup(
                    alertData = alert,
                    onDismiss = {
                        viewModel.setSelectedAlert(null)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }

            // Show GRIB popup if GRIB data is enabled
            if (showGrib && showPopup && selectedPoint != null && uiState.selectedAlert == null) {
                selectedPoint!!.data?.let {
                    AlertInfoCard(
                        alertData = it,
                        onDismiss = {
                            selectedPoint = null
                            showPopup = false
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    )
                }
            }

            // Update ship positions when they change
            LaunchedEffect(shipUiState.ships) {
                mapLibreMap?.getStyle()?.let { style ->
                    updateShipSource(context, style, shipUiState.ships)
                }
            }

            // LaunchedEffect for å håndtere polygon-visning når selectedAlert endres
            LaunchedEffect(uiState.selectedAlert) {
                mapLibreMap?.getStyle { style ->
                    updateAlertPolygon(
                        style,
                        uiState.selectedAlert?.optString("id")
                    )
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
                val inputStream: InputStream = context.assets.open(iconPath)
                inputStream.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    if (bitmap != null) {
                        val iconId = "$type-$severity"
                        style.addImage(iconId, bitmap)
                        loadedIcons++
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