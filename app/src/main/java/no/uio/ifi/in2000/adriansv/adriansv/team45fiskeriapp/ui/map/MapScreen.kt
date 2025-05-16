package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.R
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.grib.GribRepository
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.weather.WeatherDataSource
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.weather.WeatherRepository
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.fish.FishLog
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib.GribData
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.ship.Ship
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.alerts.AlertUtils
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.alerts.FarevarselPopup
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.alerts.GeoJsonViewModel
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components.BaatvettButton
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components.BaatvettOverlay
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components.ProfilePopup
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components.SettingsPopup
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components.SokeKnapp
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fish.AddFishDialog
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fish.FishLogDialog
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fish.FishLogViewModel
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fish.utils.FishLogImageUtils
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fishing.FishingTrip
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fishing.FishingTripDialog
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fishing.FishingTripStorage
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fishing.FishingTripSummaryDialog
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib.GribViewModel
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib.GribViewModelFactory
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib.overlays.GribOverlayManager
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib.utils.GribOverlayUtil
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.map.utils.LocationTrackingUtils
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.map.utils.MapScreenshotUtils
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.map.utils.WarningIconUtils
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.ship.ShipInfoCard
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.ship.ShipViewModel
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.ship.setupShipLayer
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.ship.updateShipSource
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.theme.Team45FiskeriAppTheme
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.tutorial.TutorialOverlay
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.tutorial.rememberTutorialManager
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.tutorial.tutorialTarget
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.weather.WeatherViewModel
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.weather.WeatherViewModelFactory
import org.json.JSONObject
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleOpacity
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.layers.PropertyFactory.fillColor
import org.maplibre.android.style.layers.PropertyFactory.fillOpacity
import org.maplibre.android.style.layers.PropertyFactory.fillOutlineColor
import org.maplibre.android.style.layers.PropertyFactory.iconAllowOverlap
import org.maplibre.android.style.layers.PropertyFactory.iconAnchor
import org.maplibre.android.style.layers.PropertyFactory.iconIgnorePlacement
import org.maplibre.android.style.layers.PropertyFactory.iconImage
import org.maplibre.android.style.layers.PropertyFactory.iconOffset
import org.maplibre.android.style.layers.PropertyFactory.iconOpacity
import org.maplibre.android.style.layers.PropertyFactory.iconSize
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineOpacity
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.layers.PropertyFactory.symbolPlacement
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import java.time.LocalDateTime
import java.util.Date

/**
 * HOVEDKARTSKJERMEN FOR FISKERIAPPLIKASJONEN
 * 
 * Dette er en kompleks og omfattende komponent som fungerer som applikasjonens hovedskjerm.
 * Skjermen viser et interaktivt kart med flere overlays og funksjoner tilpasset fiskere.
 * 
 * Nøkkelfunksjonalitet:
 * - Kartvisning med MapLibre (OpenStreetMap)
 * - Værvarsling og værdata-integrasjon med GRIB-data
 * - Sporing og logging av fisketurer
 * - Fangstregistrering med bilder og lokasjonsdata
 * - Farevarsel og sikkerhetsvarslinger fra Meteorologisk institutt
 * - Visning av skip/fartøy i nærheten
 * - Båtvettsregler og sikkerhetsinformasjon
 * - Interaktiv tutorial for nye brukere
 * - Adaptiv mørk/lys modus
 * 
 * Filen håndterer en rekke tilstander og brukerinteraksjoner, inkludert:
 * - Posisjonssporing og rutelogging
 * - Dialoger for fiskelogg og fisketur
 * - Kartlag for vær, fartøy og fiskesteder
 * - Brukerinteraksjon med kartet
 * 
 * Dette er en kompleks fil med tett integrasjon av mange funksjoner, og fungerer
 * som navet i applikasjonens brukergrensesnitt.
 */

private const val TAG = "MapScreen"
private const val SHIP_LAYER_ID = "ship-layer"

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun MapScreen(
    onNavigate: (String) -> Unit,
    isDarkMode: Boolean,
    showGrib: Boolean,
    showAlerts: Boolean,
    showShips: Boolean,
    onGribFilterChanged: (Boolean) -> Unit,
    onAlertsFilterChanged: (Boolean) -> Unit,
    onShipsFilterChanged: (Boolean) -> Unit,
    onLocationSelected: ((Double, Double, String) -> Unit)? = null,
    isComingFromWelcome: Boolean = false,
    onTutorialComplete: () -> Unit = {}
) {
    val weatherDataSource = WeatherDataSource()
    val weatherRepository = WeatherRepository.WeatherRepositoryImpl(weatherDataSource)
    val weatherViewModel: WeatherViewModel = viewModel(
        factory = WeatherViewModelFactory(weatherRepository)
    )

    val appContext = LocalContext.current.applicationContext
    val viewModel = remember {
        GeoJsonViewModel(appContext)
    }
    
    val shipViewModel: ShipViewModel = viewModel()
    
    // Collect states
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val shipUiState by shipViewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var mapView: MapView? by remember { mutableStateOf(null) }
    var mapLibreMap: MapLibreMap? by remember { mutableStateOf(null) }
    var selectedShip: Ship? by remember { mutableStateOf(null) }
    var selectedShipScreenPosition by remember { mutableStateOf<android.graphics.PointF?>(null) }
    val gribRepository = remember { GribRepository.GribRepositoryImpl(context) }
    
    // Filter states
    var showBaatvettRules by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var showProfilePopup by remember { mutableStateOf(false) }
    
    // User information states
    val firstName by remember { mutableStateOf("") }
    val lastName by remember { mutableStateOf("") }
    
    // Dark mode
    val isDarkTheme = isDarkMode

    // Add state for tracking loaded images
    var loadedImages by remember { mutableStateOf<Set<String>>(emptySet()) }
    var failedImageLoads by remember { mutableStateOf<Set<String>>(emptySet()) }
    var fishingTripImages by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Add FishLog ViewModel
    val fishLogViewModel = viewModel { FishLogViewModel(context) }
    val fishLogUiState by fishLogViewModel.uiState.collectAsStateWithLifecycle()
    
    // Add FishLog states
    var showFishLogDialog by remember { mutableStateOf(false) }
    var showAddFishDialog by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var showLocationSelectionDialog by remember { mutableStateOf(false) }
    var selectedLocationForFish by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    
    // Fishtrip states
    var showFishingTripDialog by remember { mutableStateOf(false) }
    var showFishingTripSummary by remember { mutableStateOf(false) }
    var fishingTripName by remember { mutableStateOf("") }
    var isFishingTripActive by remember { mutableStateOf(false) }
    var fishingTripStartTime by remember { mutableStateOf<LocalDateTime?>(null) }
    var fishingTripEndTime by remember { mutableStateOf<LocalDateTime?>(null) }
    var fishingTripStartLocation by remember { mutableStateOf<LatLng?>(null) }
    var fishingTripRoute by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var mapScreenshot by remember { mutableStateOf<Uri?>(null) }
    
    // State for AddFishDialog
    var fishType by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // TimeOut for map loading
    var mapLoadTimeout by remember { mutableStateOf(false) }
    val mapLoadTimeoutDuration = 10000L // 10 sekunder

    // Add state for tracking user location
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var isTrackingUser by remember { mutableStateOf(false) }

    // Check location permissions
    val locationPermissionState = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // State for GRIB-data
    val gribData by remember { mutableStateOf<Map<String, GribData?>>(emptyMap()) }

    val gribViewModel: GribViewModel = viewModel(
        factory = GribViewModelFactory(
            gribRepository = gribRepository,
            context = LocalContext.current
        )
    )

    val gribUiState by gribViewModel.uiState.collectAsStateWithLifecycle()

    var showNoInternetAlert by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showNoInternetAlert = !isOnline(context)
    }

    LaunchedEffect(uiState.geoJsonData) {
        mapLoadTimeout = false

        if (uiState.geoJsonData != null) {
            delay(mapLoadTimeoutDuration)
            if (mapLibreMap == null) {
                mapLoadTimeout = true
                Log.e(TAG, "Map loading timed out after ${mapLoadTimeoutDuration}ms")
            }
        }
    }
    fun clearFishLogs() {
        fishLogViewModel.clearFishLogs()
    }

    fun resetSelections() {
        viewModel.setSelectedAlert(null)
    }

    LaunchedEffect(Unit) {
        shipViewModel.startPeriodicUpdates()
    }

    val searchTarget by viewModel.searchTarget.collectAsStateWithLifecycle()
    
    LaunchedEffect(searchTarget) {
        searchTarget?.let { target ->
            val zoom = 12.0
            mapLibreMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(target.latitude, target.longitude),
                zoom
            ))
            weatherViewModel.updateWeather(target.latitude, target.longitude, zoom)
        }
    }

    LaunchedEffect(mapLibreMap) {
        mapLibreMap?.getStyle {
            val osloPosition = LatLng(59.9139, 10.7522)
            val zoom = 9.0
            mapLibreMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(osloPosition, zoom))
        }
    }

    // Helper function to reset fish dialog state
    fun resetFishDialogState() {
        fishType = ""
        location = ""
        area = ""
        description = ""
        weight = ""
        imageUri = null
        selectedLocationForFish = null
    }

    // Help function to load images for fish logs
    fun loadImage(style: Style, fishLog: FishLog) {
        FishLogImageUtils.loadImage(
            context = context,
            style = style,
            fishLog = fishLog,
            loadedImages = loadedImages,
            failedImageLoads = failedImageLoads,
            fishingTripName = fishingTripName,
            isFishingTripActive = isFishingTripActive,
            fishingTripStartTime = fishingTripStartTime,
            onImageLoaded = { imageId ->
                    loadedImages = loadedImages + imageId
                    fishLogViewModel.addLoadedImage(imageId)
            },
            onImageLoadFailed = { imageId ->
                    failedImageLoads = failedImageLoads + imageId
                    fishLogViewModel.addFailedImageLoad(imageId)
                }
        )
    }

    // Update fish log images
    LaunchedEffect(fishLogUiState.fishLogs) {
        mapLibreMap?.getStyle { style ->
            // Fjern gamle lag og kilder som ikke lenger er i bruk
            val currentImageIds = fishLogUiState.fishLogs.map { "fish_${it.timestamp}" }.toSet()
            loadedImages.filter { it !in currentImageIds }.forEach { oldImageId ->
                style.getLayer(oldImageId)?.let { style.removeLayer(it) }
                style.getSource(oldImageId)?.let { style.removeSource(it) }
                style.removeImage(oldImageId)
                fishingTripImages = fishingTripImages - oldImageId
            }
            loadedImages = loadedImages.filter { it in currentImageIds }.toSet()

            fishLogUiState.fishLogs.forEach { fishLog ->
                if (fishLog.imageUri != null) {
                    loadImage(style, fishLog)
                }
            }
        }
    }
    LaunchedEffect(mapLibreMap) {
        mapLibreMap?.getStyle { style ->
            fishLogUiState.fishLogs.forEach { fishLog ->
                if (fishLog.imageUri != null) {
                    loadImage(style, fishLog)
                }
            }
        }
    }
    LaunchedEffect(showLocationSelectionDialog) {
        if (showLocationSelectionDialog) {
            showAddFishDialog = false
        }
    }

    val tutorialManager = rememberTutorialManager()

    LaunchedEffect(tutorialManager.isCompleted) {
        if (tutorialManager.isCompleted) {
            onTutorialComplete()
        }
    }

    // This runs when the welcome screen is opened
    LaunchedEffect(isComingFromWelcome) {
        if (isComingFromWelcome) {
            tutorialManager.startTutorial()
        }
    }

    // Ask for location if necessary
    LaunchedEffect(Unit) {
        if (!locationPermissionState.value) {
            ActivityCompat.requestPermissions(
                context as android.app.Activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
    }

    LaunchedEffect(Unit) {
        val savedTripName = context.getSharedPreferences("fishing_trip", Context.MODE_PRIVATE)
            .getString("trip_name", "")
        val savedStartTime = context.getSharedPreferences("fishing_trip", Context.MODE_PRIVATE)
            .getLong("start_time", 0)
        val savedIsActive = context.getSharedPreferences("fishing_trip", Context.MODE_PRIVATE)
            .getBoolean("is_active", false)
        val savedFishingTripImages = context.getSharedPreferences("fishing_trip", Context.MODE_PRIVATE)
            .getStringSet("fishing_trip_images", emptySet()) ?: emptySet()

        if (savedIsActive && savedStartTime > 0) {
            fishingTripName = savedTripName ?: ""
            isFishingTripActive = true
            fishingTripStartTime = LocalDateTime.ofEpochSecond(savedStartTime, 0, java.time.ZoneOffset.UTC)
        }
        fishingTripImages = savedFishingTripImages
    }

    // Save fishtrip data
    LaunchedEffect(isFishingTripActive, fishingTripStartTime, fishingTripName, fishingTripImages) {
        context.getSharedPreferences("fishing_trip", Context.MODE_PRIVATE).edit().apply {
            putString("trip_name", fishingTripName)
            putLong("start_time", fishingTripStartTime?.toEpochSecond(java.time.ZoneOffset.UTC) ?: 0)
            putBoolean("is_active", isFishingTripActive)
            putStringSet("fishing_trip_images", fishingTripImages)
            apply()
        }
    }

    // Update users posisjon
    LaunchedEffect(mapLibreMap) {
        mapLibreMap?.getStyle { style ->
            try {
                val userLocationSource = GeoJsonSource("user-location-source")
                style.addSource(userLocationSource)

                val routeSource = GeoJsonSource("fishing-trip-route-source")
                style.addSource(routeSource)

                val routeLayer = LineLayer("fishing-trip-route-layer", "fishing-trip-route-source")
                    .withProperties(
                        lineColor(Color.YELLOW),
                        lineWidth(4f),
                        lineOpacity(0.8f)
                    )
                style.addLayer(routeLayer)

                // Blue circle for user location
                val userLocationLayer = CircleLayer("user-location-layer", "user-location-source")
                    .withProperties(
                        circleRadius(4f),
                        circleColor(Color.BLUE),
                        circleOpacity(0.9f),
                        circleStrokeWidth(1f),
                        circleStrokeColor(Color.WHITE)
                    )
                style.addLayer(userLocationLayer)

                LocationTrackingUtils.setupLocationTracking(
                    context = context,
                    mapLibreMap = mapLibreMap,
                    userLocationSource = userLocationSource,
                    routeSource = routeSource,
                    isTrackingUser = isTrackingUser,
                    isFishingTripActive = isFishingTripActive,
                    fishingTripRoute = fishingTripRoute,
                    onLocationUpdate = { newLocation ->
                        userLocation = newLocation
                    },
                    onRouteUpdate = { newRoute ->
                        fishingTripRoute = newRoute
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error setting up location tracking: ${e.message}")
            }
        }
    }

    LaunchedEffect(isFishingTripActive) {
        if (isFishingTripActive) {
            isTrackingUser = true
            userLocation?.let { location ->
            mapLibreMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(location, 18.0)
                )
            }
        } else {
            isTrackingUser = false
        }
    }

    // Initialize GribOverlayUtil with saved thresholds
    LaunchedEffect(Unit) {
        GribOverlayUtil.initialize(context)
    }

    // Call viewmodel to load grib data
    LaunchedEffect(showGrib) {
        if (showGrib) {
            gribViewModel.loadGribOverlayData()
        }
    }

    // Call overlay-manager to add grib overlay
    LaunchedEffect(mapLibreMap, gribUiState.geoJson, isDarkMode) {
        val geoJson = gribUiState.geoJson
        if (mapLibreMap != null && geoJson != null && showGrib) {
            mapLibreMap?.getStyle { style ->
                GribOverlayManager.addOrUpdateGribOverlay(context, style, geoJson, isDarkMode)
            }
        }
    }

    Team45FiskeriAppTheme(darkTheme = isDarkTheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Søkeknapp plassert øverst på skjermen
            SokeKnapp(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .zIndex(1f)
                    .tutorialTarget(),
                onQueryChange = { query ->
                    viewModel.getSearchSuggestions(query)
                },
                suggestions = uiState.searchSuggestions,
                onSuggestionSelected = { suggestion ->
                    viewModel.selectSuggestion(suggestion)
                }
            )

            uiState.error?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }

            // Kartet skal alltid vises, uansett nettstatus eller geoJsonData
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).also { view ->
                        mapView = view
                        try {
                            view.getMapAsync { map ->
                                try {
                                    mapLibreMap = map
                                    val styleUrl = if (isDarkMode) {
                                        "https://api.maptiler.com/maps/streets-v2-dark/style.json?key=oMZQoq4zniKOHeMvi7oA"
                                    } else {
                                        "https://api.maptiler.com/maps/streets-v2/style.json?key=oMZQoq4zniKOHeMvi7oA"
                                    }
                                    map.setStyle(Style.Builder().fromUri(styleUrl)) { style ->
                                        try {
                                            // Nullstill timeout siden kartet er lastet
                                            mapLoadTimeout = false

                                            // Set initial camera position to Oslo Fjord
                                            val osloPosition = LatLng(59.9139, 10.7522)
                                            val position = CameraPosition.Builder()
                                                .target(osloPosition)
                                                .zoom(9.0)
                                                .build()
                                            map.moveCamera(
                                                CameraUpdateFactory.newCameraPosition(
                                                    position
                                                )
                                            )

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
                                            WarningIconUtils.loadWarningIcons(context, style)
                                            setupShipLayer(context, style)

                                            // overlays og lag kan fortsatt sjekke geoJsonData
                                            if (uiState.geoJsonData != null) {
                                                // overlays, lag, etc.
                                                val source = GeoJsonSource("alerts-source", uiState.geoJsonData)
                                                style.addSource(source)

                                                val symbolLayer =
                                                    SymbolLayer("alert-symbol-layer", "alerts-source")
                                                        .withProperties(
                                                            iconImage(
                                                                Expression.match(
                                                                    Expression.get("eventAwarenessName"),
                                                                    Expression.literal("Sterk ising på skip"),
                                                                    Expression.concat(
                                                                        Expression.literal("generic-"),
                                                                        Expression.match(
                                                                            Expression.get("severity"),
                                                                            Expression.literal("Severe"),
                                                                            Expression.literal("red"),
                                                                            Expression.literal("Moderate"),
                                                                            Expression.literal("orange"),
                                                                            Expression.literal("Minor"),
                                                                            Expression.literal("yellow"),
                                                                            Expression.literal("yellow")
                                                                        )
                                                                    ),
                                                                    Expression.literal("Storm"),
                                                                    Expression.concat(
                                                                        Expression.literal("wind-"),
                                                                        Expression.match(
                                                                            Expression.get("severity"),
                                                                            Expression.literal("Severe"),
                                                                            Expression.literal("red"),
                                                                            Expression.literal("Moderate"),
                                                                            Expression.literal("orange"),
                                                                            Expression.literal("Minor"),
                                                                            Expression.literal("yellow"),
                                                                            Expression.literal("yellow")
                                                                        )
                                                                    ),
                                                                    Expression.literal("Kuling"),
                                                                    Expression.concat(
                                                                        Expression.literal("wind-"),
                                                                        Expression.match(
                                                                            Expression.get("severity"),
                                                                            Expression.literal("Severe"),
                                                                            Expression.literal("red"),
                                                                            Expression.literal("Moderate"),
                                                                            Expression.literal("orange"),
                                                                            Expression.literal("Minor"),
                                                                            Expression.literal("yellow"),
                                                                            Expression.literal("yellow")
                                                                        )
                                                                    ),
                                                                    Expression.literal("Kraftige vindkast"),
                                                                    Expression.concat(
                                                                        Expression.literal("wind-"),
                                                                        Expression.match(
                                                                            Expression.get("severity"),
                                                                            Expression.literal("Severe"),
                                                                            Expression.literal("red"),
                                                                            Expression.literal("Moderate"),
                                                                            Expression.literal("orange"),
                                                                            Expression.literal("Minor"),
                                                                            Expression.literal("yellow"),
                                                                            Expression.literal("yellow")
                                                                        )
                                                                    ),
                                                                    Expression.literal("Skogbrannfare"),
                                                                    Expression.concat(
                                                                        Expression.literal("forestfire-"),
                                                                        Expression.match(
                                                                            Expression.get("severity"),
                                                                            Expression.literal("Severe"),
                                                                            Expression.literal("red"),
                                                                            Expression.literal("Moderate"),
                                                                            Expression.literal("orange"),
                                                                            Expression.literal("Minor"),
                                                                            Expression.literal("yellow"),
                                                                            Expression.literal("yellow")
                                                                        )
                                                                    ),
                                                                    Expression.literal("generic-yellow")
                                                                )
                                                            ),
                                                            iconSize(0.8f),
                                                            iconAllowOverlap(true),
                                                            iconIgnorePlacement(true),
                                                            iconOffset(arrayOf(0f, -5f)),
                                                            symbolPlacement(Property.SYMBOL_PLACEMENT_POINT),
                                                            iconAnchor(Property.ICON_ANCHOR_CENTER)
                                                        )
                                                style.addLayer(symbolLayer)

                                                val polygonLayer =
                                                    FillLayer("alert-polygon-layer", "alerts-source")
                                                        .withProperties(
                                                            fillColor(
                                                                Expression.match(
                                                                    Expression.get("severity"),
                                                                    Expression.literal("Severe"),
                                                                    Expression.color("#66D32F2F".toColorInt()),
                                                                    Expression.literal("Moderate"),
                                                                    Expression.color("#66F57C00".toColorInt()),
                                                                    Expression.literal("Minor"),
                                                                    Expression.color("#66FBC02D".toColorInt()),
                                                                    Expression.color("#66FBC02D".toColorInt())
                                                                )
                                                            ),
                                                            fillOpacity(0f),
                                                            fillOutlineColor("#000000".toColorInt())
                                                        )
                                                        .withFilter(
                                                            Expression.any(
                                                                Expression.eq(
                                                                    Expression.geometryType(),
                                                                    Expression.literal("Polygon")
                                                                ),
                                                                Expression.eq(
                                                                    Expression.geometryType(),
                                                                    Expression.literal("MultiPolygon")
                                                                )
                                                            )
                                                        )

                                                style.addLayerBelow(polygonLayer, "alert-symbol-layer")
                                            }

                                            map.addOnMapClickListener { point ->
                                                val screenPoint =
                                                    map.projection.toScreenLocation(point)

                                                if (onLocationSelected != null) {
                                                    onLocationSelected(
                                                        point.latitude,
                                                        point.longitude,
                                                        "Valgt lokasjon"
                                                    )
                                                    return@addOnMapClickListener true
                                                }

                                                val fishFeatures =
                                                    map.queryRenderedFeatures(screenPoint)
                                                val fishLog = fishFeatures.find { feature ->
                                                    val properties = feature.properties()
                                                    if (properties != null) {
                                                        val timestamp =
                                                            properties.get("timestamp")?.asString
                                                        if (timestamp != null) {
                                                            fishLogUiState.fishLogs.find { it.timestamp.toString() == timestamp } != null
                                                        } else false
                                                    } else false
                                                }?.let { feature ->
                                                    val properties = feature.properties()
                                                    val timestamp =
                                                        properties?.get("timestamp")?.asString
                                                    if (timestamp != null) {
                                                        fishLogUiState.fishLogs.find { it.timestamp.toString() == timestamp }
                                                    } else null
                                                }

                                                if (fishLog != null) {
                                                    selectedLocation =
                                                        Pair(fishLog.latitude, fishLog.longitude)
                                                    showFishLogDialog = true
                                                    resetSelections() // Nullstill GRIB-data og andre popups
                                                    return@addOnMapClickListener true
                                                }

                                                if (showAlerts) {
                                                    val alertFeatures = map.queryRenderedFeatures(
                                                        screenPoint,
                                                        "alert-symbol-layer"
                                                    )
                                                    if (alertFeatures.isNotEmpty()) {
                                                        val feature = alertFeatures[0]
                                                        val properties = feature.properties()
                                                        if (properties != null) {
                                                            val jsonObject =
                                                                JSONObject(properties.toString())
                                                            val id = properties.get("id").asString
                                                            resetSelections()
                                                            viewModel.setSelectedAlert(jsonObject)

                                                            // Vis polygon for dette varselet
                                                            map.getStyle { style ->
                                                                AlertUtils.updateAlertPolygon(
                                                                    style,
                                                                    id
                                                                )
                                                            }
                                                        }
                                                        return@addOnMapClickListener true
                                                    }
                                                }

                                                if (showShips) {
                                                    val shipFeatures = map.queryRenderedFeatures(
                                                        screenPoint,
                                                        SHIP_LAYER_ID
                                                    )
                                                    if (shipFeatures.isNotEmpty()) {
                                                        resetSelections()
                                                        val feature = shipFeatures[0]
                                                        val properties = feature.properties()

                                                        if (properties != null) {
                                                            val mmsi =
                                                                properties.get("mmsi")?.asString
                                                            if (mmsi != null) {
                                                                selectedShip =
                                                                    shipUiState.ships.find { it.mmsi == mmsi }
                                                                if (selectedShip != null) {
                                                                    selectedShipScreenPosition =
                                                                        mapLibreMap?.projection?.toScreenLocation(
                                                                            LatLng(
                                                                                selectedShip!!.latitude,
                                                                                selectedShip!!.longitude
                                                                            )
                                                                        )
                                                                }
                                                            }
                                                        }

                                                        map.getStyle { style ->
                                                            AlertUtils.updateAlertPolygon(
                                                                style,
                                                                null
                                                            )
                                                        }
                                                        return@addOnMapClickListener true
                                                    }
                                                }

                                                if (showLocationSelectionDialog) {
                                                    selectedLocationForFish =
                                                        Pair(point.latitude, point.longitude)
                                                    showLocationSelectionDialog = false
                                                    showAddFishDialog = true
                                                    selectedLocation =
                                                        Pair(point.latitude, point.longitude)
                                                    return@addOnMapClickListener true
                                                }

                                                false
                                            }

                                            // Update layer visibility based on filters
                                            style.getLayer("alert-symbol-layer")?.setProperties(
                                                iconOpacity(Expression.literal(if (showAlerts) 1f else 0f))
                                            )
                                            style.getLayer(SHIP_LAYER_ID)?.setProperties(
                                                iconOpacity(Expression.literal(if (showShips) 1f else 0f))
                                            )

                                            if (showGrib) {

                                                gribData["wind"]?.let { windData ->
                                                    val windGeoJson =
                                                        GribOverlayUtil.gribDataToFeatureCollection(
                                                            windData,
                                                            "wind",
                                                            "wind",
                                                            minDistanceKm = 10.0
                                                        )
                                                    val windSource =
                                                        GeoJsonSource("wind-source", windGeoJson)
                                                    style.addSource(windSource)

                                                    val windLayer =
                                                        CircleLayer("wind-layer", "wind-source")
                                                            .withProperties(
                                                                circleRadius(4f),
                                                                circleColor(Expression.get("color")),
                                                                circleOpacity(0.8f)
                                                            )
                                                    style.addLayer(windLayer)
                                                }

                                                gribData["wave"]?.let { waveData ->
                                                    val waveGeoJson =
                                                        GribOverlayUtil.gribDataToFeatureCollection(
                                                            waveData,
                                                            "wave",
                                                            "wave",
                                                            minDistanceKm = 10.0
                                                        )
                                                    val waveSource =
                                                        GeoJsonSource("wave-source", waveGeoJson)
                                                    style.addSource(waveSource)

                                                    val waveLayer =
                                                        CircleLayer("wave-layer", "wave-source")
                                                            .withProperties(
                                                                circleRadius(4f),
                                                                circleColor(Expression.get("color")),
                                                                circleOpacity(0.8f)
                                                            )
                                                    style.addLayer(waveLayer)
                                                }

                                                gribData["strom"]?.let { currentData ->
                                                    val currentGeoJson =
                                                        GribOverlayUtil.gribDataToFeatureCollection(
                                                            currentData,
                                                            "strom",
                                                            "strom",
                                                            minDistanceKm = 10.0
                                                        )
                                                    val currentSource = GeoJsonSource(
                                                        "current-source",
                                                        currentGeoJson
                                                    )
                                                    style.addSource(currentSource)

                                                    val currentLayer = CircleLayer(
                                                        "current-layer",
                                                        "current-source"
                                                    )
                                                        .withProperties(
                                                            circleRadius(4f),
                                                            circleColor(Expression.get("color")),
                                                            circleOpacity(0.8f)
                                                        )
                                                    style.addLayer(currentLayer)
                                                }

                                                gribData["rain"]?.let { rainData ->
                                                    val precipitationGeoJson =
                                                        GribOverlayUtil.gribDataToFeatureCollection(
                                                            rainData,
                                                            "rain",
                                                            "rain",
                                                            minDistanceKm = 10.0
                                                        )
                                                    val precipitationSource = GeoJsonSource(
                                                        "precipitation-source",
                                                        precipitationGeoJson
                                                    )
                                                    style.addSource(precipitationSource)

                                                    val precipitationLayer = CircleLayer(
                                                        "precipitation-layer",
                                                        "precipitation-source"
                                                    )
                                                        .withProperties(
                                                            circleRadius(4f),
                                                            circleColor(Expression.get("color")),
                                                            circleOpacity(0.8f)
                                                        )
                                                    style.addLayer(precipitationLayer)
                                                }
                                            } else {
                                                try {
                                                    style.getLayer("wind-layer")
                                                        ?.let { style.removeLayer(it) }
                                                    style.getSource("wind-source")
                                                        ?.let { style.removeSource(it) }
                                                    style.getLayer("wave-layer")
                                                        ?.let { style.removeLayer(it) }
                                                    style.getSource("wave-source")
                                                        ?.let { style.removeSource(it) }
                                                    style.getLayer("current-layer")
                                                        ?.let { style.removeLayer(it) }
                                                    style.getSource("current-source")
                                                        ?.let { style.removeSource(it) }
                                                    style.getLayer("precipitation-layer")
                                                        ?.let { style.removeLayer(it) }
                                                    style.getSource("precipitation-source")
                                                        ?.let { style.removeSource(it) }
                                                } catch (_: Exception) {

                                                }
                                            }
                                        } catch (e: Exception) {
                                            Log.e(TAG, "Error setting up map layers: ${e.message}")

                                            map.triggerRepaint()
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error setting up map: ${e.message}")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error getting map async: ${e.message}")
                        }
                    }
                },
                update = { mapView ->
                    if (mapLoadTimeout && mapLibreMap == null) {
                        try {
                            mapView.getMapAsync { map ->
                                mapLibreMap = map
                                map.setStyle(Style.Builder().fromUri("https://api.maptiler.com/maps/streets-v2/style.json?key=oMZQoq4zniKOHeMvi7oA"))
                            }
                            mapLoadTimeout = false
                        } catch (e: Exception) {
                            Log.e(TAG, "Error reloading map: ${e.message}")
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // UI Elements
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // "Fisketur" button
                FloatingActionButton(
                    onClick = { showFishingTripDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 143.dp)
                        .tutorialTarget(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ftr),
                        contentDescription = "Start fisketur",
                        modifier = Modifier
                            .size(60.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                FloatingActionButton(
                    onClick = { 
                        showFishLogDialog = true
                        selectedLocation = null
                        selectedShip = null
                        selectedShipScreenPosition = null
                        showLocationSelectionDialog = false
                        showAddFishDialog = false
                        selectedLocationForFish = null
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 80.dp)
                        .tutorialTarget(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.flk),
                        contentDescription = "Fiskelogg",
                        modifier = Modifier
                            .size(59.dp)
                            .padding(4.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                BaatvettButton(
                    onClick = { showBaatvettRules = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 16.dp)
                )
            }
            

            if (showBaatvettRules) {
                BaatvettOverlay(
                    onDismiss = { showBaatvettRules = false }
                )
            }

            if (showFilterMenu) {
                SettingsPopup(
                    context = context,
                    isDarkMode = isDarkMode,
                    showGrib = showGrib,
                    showAlerts = showAlerts,
                    showShips = showShips,
                    onDarkModeChange = {
                        // Handle dark mode change
                        onNavigate("kart") // Refresh the screen with new theme
                    },
                    onGribFilterChanged = onGribFilterChanged,
                    onAlertsFilterChanged = onAlertsFilterChanged,
                    onShipsFilterChanged = onShipsFilterChanged,
                    onDismiss = { showFilterMenu = false },
                    gribViewModel = gribViewModel
                )
            }

            if (showProfilePopup) {
                ProfilePopup(
                    userName = "$firstName $lastName",
                    onSettingsClick = {
                        showFilterMenu = true
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

            uiState.selectedAlert?.let { alert ->
                FarevarselPopup(
                    alertData = alert,
                    onDismiss = {
                        viewModel.setSelectedAlert(null)
                        mapLibreMap?.getStyle { style ->
                            AlertUtils.updateAlertPolygon(style, null)
                        }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    )
                }

            // Update ship positions when they change
            LaunchedEffect(shipUiState.ships) {
                mapLibreMap?.style?.let { style ->
                    updateShipSource(context, style, shipUiState.ships)
                }
            }

            // Show fish log dialog
            if (showFishLogDialog) {
                FishLogDialog(
                    fishLogs = fishLogUiState.fishLogs,
                    onDismiss = { 
                        showFishLogDialog = false
                        selectedLocation = null
                    },
                    onClearLogs = { 
                        clearFishLogs()
                        // Fjern alle fiskelag og kilder når loggen tømmes
                        mapLibreMap?.getStyle { style ->
                            fishLogUiState.fishLogs.forEach { fishLog ->
                                style.getLayer("fish_${fishLog.timestamp}")?.let { style.removeLayer(it) }
                                style.getSource("fish_${fishLog.timestamp}")?.let { style.removeSource(it) }
                                style.removeImage("fish_${fishLog.timestamp}")
                            }
                        }
                        loadedImages = emptySet()
                        failedImageLoads = emptySet()
                    },
                    onAddFish = {
                        showFishLogDialog = false
                        showAddFishDialog = true
                        selectedLocation = null
                        selectedLocationForFish = null
                    },
                    selectedLocation = selectedLocation,
                    onRemoveFish = { fishLog ->
                        fishLogViewModel.removeFishLog(fishLog)

                        mapLibreMap?.getStyle { style ->
                            style.getLayer("fish_${fishLog.timestamp}")?.let { style.removeLayer(it) }
                            style.getSource("fish_${fishLog.timestamp}")?.let { style.removeSource(it) }
                            style.removeImage("fish_${fishLog.timestamp}")
                        }
                        
                        // Oppdater loadedImages og failedImageLoads
                        loadedImages = loadedImages - "fish_${fishLog.timestamp}"
                        failedImageLoads = failedImageLoads - "fish_${fishLog.timestamp}"
                    }
                )
            }

            // Show add fish dialog
            if (showAddFishDialog) {
                AddFishDialog(
                    onDismiss = { 
                        showAddFishDialog = false
                        resetFishDialogState()
                        selectedLocation = null
                    },
                    onAddFish = { fishLog ->
                        fishLogViewModel.addFishLog(fishLog)
                        showAddFishDialog = false
                        resetFishDialogState()
                        selectedLocation = null
                    },
                    latitude = selectedLocationForFish?.first ?: 59.9139,
                    longitude = selectedLocationForFish?.second ?: 10.7522,
                    onSelectLocation = {
                        showAddFishDialog = false
                        showLocationSelectionDialog = true
                        selectedLocation = null
                        selectedShip = null
                        selectedShipScreenPosition = null
                    },
                    initialFishType = fishType,
                    initialArea = area,
                    initialDescription = description,
                    initialWeight = weight,
                    initialImageUri = imageUri,
                    onFishTypeChange = { fishType = it },
                    onAreaChange = { area = it },
                    onDescriptionChange = { description = it },
                    onWeightChange = { weight = it },
                    onImageUriChange = { imageUri = it },
                    selectedLocationForFish = selectedLocationForFish?.toString()
                )
            }

            if (showFishingTripDialog) {
                FishingTripDialog(
                    tripName = fishingTripName,
                    onTripNameChange = { fishingTripName = it },
                    isTripActive = isFishingTripActive,
                    onStartTrip = { location ->
                        isFishingTripActive = true
                        fishingTripStartTime = LocalDateTime.now()
                        fishingTripStartLocation = LatLng(location.latitude, location.longitude)
                        // Start tracking brukerens posisjon og initialiser ruten
                        isTrackingUser = true
                        fishingTripRoute = listOf(LatLng(location.latitude, location.longitude))
                        userLocation = LatLng(location.latitude, location.longitude)
                        mapLibreMap?.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(location.latitude, location.longitude),
                                18.0
                            )
                        )
                        showFishingTripDialog = false
                    },
                    onEndTrip = {
                        isFishingTripActive = false
                        fishingTripEndTime = LocalDateTime.now()
                        isTrackingUser = false

                        mapLibreMap?.snapshot { bitmap ->
                            mapScreenshot = MapScreenshotUtils.saveMapScreenshot(context, bitmap)
                        }

                        showFishingTripDialog = false
                        showFishingTripSummary = true
                    },
                    startTime = fishingTripStartTime,
                    onClose = { showFishingTripDialog = false },

                    fishLogViewModel = fishLogViewModel
                )
            }

            if (showFishingTripSummary && fishingTripStartTime != null && fishingTripEndTime != null) {
                val catchesForTrip = fishLogUiState.fishLogs.filter {
                    val logDate = it.timestamp
                    val startDate = Date.from(fishingTripStartTime!!.atZone(java.time.ZoneId.systemDefault()).toInstant())
                    val endDate = Date.from(fishingTripEndTime!!.atZone(java.time.ZoneId.systemDefault()).toInstant())
                    logDate.after(startDate) && logDate.before(endDate)
                }

                FishingTripSummaryDialog(
                    onDismiss = {
                        val startTimeMillis = fishingTripStartTime!!.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                        val endTimeMillis = fishingTripEndTime!!.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

                        if (mapScreenshot != null) {
                            val fishingTrip = FishingTrip(
                                name = fishingTripName,
                                screenshotUri = mapScreenshot.toString(),
                                startTime = startTimeMillis,
                                endTime = endTimeMillis,
                                catchCount = catchesForTrip.size
                            )

                            FishingTripStorage.saveTrip(context, fishingTrip)
                        }

                        showFishingTripSummary = false
                    },
                    tripName = fishingTripName,
                    startTime = fishingTripStartTime!!,
                    endTime = fishingTripEndTime!!,
                    catches = catchesForTrip,
                    mapScreenshot = mapScreenshot
                )
            }

            TutorialOverlay(
                state = tutorialManager.tutorialState,
                onNext = { tutorialManager.nextStep() },
                onSkip = { tutorialManager.skipTutorial() }
            )

            if (showNoInternetAlert) {
                AlertDialog(
                    onDismissRequest = { showNoInternetAlert = false },
                    title = { Text("Ingen internettforbindelse") },
                    text = { Text("Kartet vises, men du er ikke tilkoblet internett. Nye kartdata kan ikke lastes inn.") },
                    confirmButton = {
                        TextButton(onClick = { showNoInternetAlert = false }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mapView?.onStop()
            mapView?.onDestroy()
                mapLibreMap = null
            } catch (e: Exception) {
                Log.e(TAG, "Error disposing map: ${e.message}")
            }
        }
    }


    LaunchedEffect(mapView) {
        if (mapView != null) {
            try {
                mapView?.onStart()
                mapView?.onResume()
            } catch (e: Exception) {
                Log.e(TAG, "Error in map lifecycle management: ${e.message}")
            }
        }
    }
}

fun isOnline(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(network)
    return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
} 