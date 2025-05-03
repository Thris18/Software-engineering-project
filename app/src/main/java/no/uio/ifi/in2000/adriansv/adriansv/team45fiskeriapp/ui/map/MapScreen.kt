package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.map

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
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
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components.SokeKnapp
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
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import android.graphics.Point
import androidx.compose.material.icons.filled.Add
import com.google.android.gms.common.Feature
import org.maplibre.android.style.layers.PropertyFactory.*
import android.net.Uri
import androidx.compose.foundation.Image
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fish.FishLogViewModel
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fish.FishLogDialog
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fish.AddFishDialog
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.fish.FishLog
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.R
import androidx.compose.ui.graphics.ColorFilter
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components.NavigationBar
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather.LocationWeather
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.weather.WeatherUiState
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.tutorial.*
import androidx.compose.ui.unit.DpOffset
import kotlinx.coroutines.delay
import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.graphics.Bitmap
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.time.LocalDateTime
import java.util.*
import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fishing.FishingTrip
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fishing.FishingTripStorage
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fishing.FishingTripDialog
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fishing.FishingTripSummaryDialog
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import java.io.File
import java.io.FileOutputStream

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

@RequiresApi(Build.VERSION_CODES.O)
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
    onShipsFilterChanged: (Boolean) -> Unit,
    onLocationSelected: ((Double, Double, String) -> Unit)? = null,
    isComingFromWelcome: Boolean = false,
    onTutorialComplete: () -> Unit = {}
) {
    val weatherDataSource = WeatherDataSource()
    val weatherRepository = WeatherRepository(weatherDataSource)
    val weatherViewModel: WeatherViewModel = viewModel(
        factory = WeatherViewModelFactory(weatherRepository)
    )
    
    // Oppretter GeoJsonViewModel direkte med context
    val appContext = LocalContext.current.applicationContext
    val viewModel = remember {
        GeoJsonViewModel(appContext)
    }
    
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
    var showFilterMenu by remember { mutableStateOf(false) }
    var showProfilePopup by remember { mutableStateOf(false) }
    
    // Brukerinformasjon states
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    
    // Sett opp dark mode
    val isDarkTheme = isDarkMode

    // Add state for tracking loaded images
    var loadedImages by remember { mutableStateOf<Set<String>>(emptySet()) }
    var failedImageLoads by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Add FishLog ViewModel
    val fishLogViewModel = viewModel { FishLogViewModel(context) }
    val fishLogUiState by fishLogViewModel.uiState.collectAsStateWithLifecycle()
    
    // Add FishLog states
    var showFishLogDialog by remember { mutableStateOf(false) }
    var showAddFishDialog by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var showActionDialog by remember { mutableStateOf(false) }
    var showLocationSelectionDialog by remember { mutableStateOf(false) }
    var selectedLocationForFish by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    
    // Fisketur states
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
    
    // Legg til states for brukerens posisjon
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var isTrackingUser by remember { mutableStateOf(false) }

    // Sjekk lokasjonstillatelser
    val locationPermissionState = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    LaunchedEffect(uiState.geoJsonData) {
        // Reset timeout når geoJsonData oppdateres
        mapLoadTimeout = false
        
        // Start timeout timer
        if (uiState.geoJsonData != null) {
            delay(mapLoadTimeoutDuration)
            if (mapLibreMap == null) {
                // Kartet lastet ikke innen tidsgrensen
                mapLoadTimeout = true
                Log.e(TAG, "Map loading timed out after ${mapLoadTimeoutDuration}ms")
            }
        }
    }

    // Tøm fiskeloggen når man tømmer loggen i kartet
    fun clearFishLogs() {
        fishLogViewModel.clearFishLogs()
    }

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

    // Observer for søkeresultat
    val searchTarget by viewModel.searchTarget.collectAsStateWithLifecycle()
    
    // Oppdater værvarsel når kameraet flyttes
    LaunchedEffect(mapLibreMap) {
        mapLibreMap?.addOnCameraIdleListener {
            val center = mapLibreMap?.cameraPosition?.target
            val zoom = mapLibreMap?.cameraPosition?.zoom ?: 12.0
            if (center != null) {
                Log.d(TAG, "Oppdaterer værvarsel med koordinater: ${center.latitude}, ${center.longitude}, zoom: $zoom")
                weatherViewModel.updateWeather(center.latitude, center.longitude, zoom)
            }
        }
    }

    // Oppdater værvarsel når søkeresultat er tilgjengelig
    LaunchedEffect(searchTarget) {
        if (searchTarget != null && mapLibreMap != null) {
            val zoom = 12.0
            Log.d(TAG, "Oppdaterer værvarsel med søkeresultat: ${searchTarget!!.latitude}, ${searchTarget!!.longitude}, zoom: $zoom")
            mapLibreMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(searchTarget!!, zoom))
            weatherViewModel.updateWeather(searchTarget!!.latitude, searchTarget!!.longitude, zoom)
        }
    }

    // Oppdater værvarsel når kartet er lastet
    LaunchedEffect(mapLibreMap) {
        mapLibreMap?.getStyle { style ->
            val osloPosition = LatLng(59.9139, 10.7522)
            val zoom = 9.0
            Log.d(TAG, "Oppdaterer værvarsel med initial posisjon: ${osloPosition.latitude}, ${osloPosition.longitude}, zoom: $zoom")
            weatherViewModel.updateWeather(osloPosition.latitude, osloPosition.longitude, zoom)
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

    // Helper function to load image
    fun loadImage(style: Style, fishLog: FishLog) {
        val imageId = "fish_${fishLog.timestamp}"
        
        // Skip if already loaded or failed
        if (imageId in loadedImages || imageId in failedImageLoads) {
            return
        }

        try {
            val uri = Uri.parse(fishLog.imageUri)
            val image = if (uri.scheme == "file") {
                BitmapFactory.decodeFile(uri.path)
            } else {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }
            
            if (image != null) {
                try {
                    // Legg til bildet
                    style.addImage(imageId, image)
                    loadedImages = loadedImages + imageId
                    fishLogViewModel.addLoadedImage(imageId)
                    
                    // Legg til GeoJSON kilde
                    val source = GeoJsonSource(
                        "fish_${fishLog.timestamp}",
                        "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[${fishLog.longitude},${fishLog.latitude}]},\"properties\":{\"timestamp\":\"${fishLog.timestamp}\"}}"
                    )
                    style.addSource(source)
                    
                    // Legg til symbol layer
                    val layer = SymbolLayer("fish_${fishLog.timestamp}", "fish_${fishLog.timestamp}")
                        .withProperties(
                            iconImage(imageId),
                            iconSize(0.05f),
                            iconAllowOverlap(true),
                            iconIgnorePlacement(true),
                            iconAnchor(Property.ICON_ANCHOR_CENTER),
                            iconOpacity(0.8f)
                        )
                    style.addLayer(layer)
                } catch (e: Exception) {
                    Log.e(TAG, "Feil ved lasting av bilde: ${e.message}")
                    failedImageLoads = failedImageLoads + imageId
                    fishLogViewModel.addFailedImageLoad(imageId)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Feil ved lasting av bilde: ${e.message}")
            failedImageLoads = failedImageLoads + imageId
            fishLogViewModel.addFailedImageLoad(imageId)
        }
    }

    // Håndterer klikk på kartet
    fun onMapClick(latLng: LatLng) {
        if (onLocationSelected != null && showLocationSelectionDialog) {
            // Hvis vi er i lokasjonsvalg-modus, send valgt lokasjon tilbake
            onLocationSelected(latLng.latitude, latLng.longitude, "Valgt lokasjon")
            showLocationSelectionDialog = false
            return
        }

        // Normal kartinteraksjon
        if (!showLocationSelectionDialog) {
            selectedPoint = null
            showPopup = false
            viewModel.setSelectedAlert(null)
        }
    }

    // Oppdater fiskelogg-bilder på kartet
    LaunchedEffect(fishLogUiState.fishLogs) {
        mapLibreMap?.getStyle { style ->
            // Fjern gamle lag og kilder som ikke lenger er i bruk
            val currentImageIds = fishLogUiState.fishLogs.map { "fish_${it.timestamp}" }.toSet()
            loadedImages.filter { it !in currentImageIds }.forEach { oldImageId ->
                style.getLayer(oldImageId)?.let { style.removeLayer(it) }
                style.getSource(oldImageId)?.let { style.removeSource(it) }
                style.removeImage(oldImageId)
            }
            loadedImages = loadedImages.filter { it in currentImageIds }.toSet()
            
            // Legg til nye fiskelag
            fishLogUiState.fishLogs.forEach { fishLog ->
                if (fishLog.imageUri != null) {
                    loadImage(style, fishLog)
                }
            }
        }
    }

    // Oppdater fiskelogg-bilder når kartet er lastet
    LaunchedEffect(mapLibreMap) {
        mapLibreMap?.getStyle { style ->
            fishLogUiState.fishLogs.forEach { fishLog ->
                if (fishLog.imageUri != null) {
                    loadImage(style, fishLog)
                }
            }
        }
    }

    // Håndter lokasjonsvalg
    LaunchedEffect(showLocationSelectionDialog) {
        if (showLocationSelectionDialog) {
            showAddFishDialog = false
        }
    }

    // --- Tutorial ---
    val tutorialManager = rememberTutorialManager()
    val tutorialSteps = remember {
        listOf(
            TutorialStep(
                title = "Velkommen til appen!",
                description = "Dette er en rask introduksjon som hjelper deg å forstå appen.",
                targetTag = "",
                mascotResourceId = R.drawable.presenting
            ),
            TutorialStep(
                title = "Kartvisning",
                description = "Her på kartet kan du utforske fiskesteder, interagere med andre fartøy, og bli varslet om fare- og værvarsel." ,
                targetTag = "",
                mascotResourceId = R.drawable.nedvenstre
            ),
            TutorialStep(
                title = "Profilsiden",
                description = "Her kan du lage din profil, endre dine innstillinger og loggføre dine favorittfangster!",
                targetTag = "",
                mascotResourceId = R.drawable.nedhoyre
            ),
            TutorialStep(
                title = "Søkefunksjonen",
                description = "Lyst til å planlegge området først? Søkefunksjonen hjelper deg å finne fram til der du ønsker å dra!",
                targetTag = "search_button",
                mascotResourceId = R.drawable.pekopp
            ),
            TutorialStep(
                title = "Båtvettregler",
                description = "Før du ferder på sjøen, er det viktig å vite om reglene!",
                targetTag = "",
                mascotResourceId = R.drawable.tilhoyre
            ),
            TutorialStep(
                title = "Fiskeloggen",
                description = "I Fiskeloggen kan du lagre dine fisker og plassere de på kartet der du fikk de!",
                targetTag = "fish_log_button",
                mascotResourceId = R.drawable.tilhoyre
            ),
            TutorialStep(
                title = "Fisketuren",
                description = "Trykk her for å starte en fisketur. Appen holder styr på tiden og fangstene dine, som du kan finne igjen i Min Profil!",
                targetTag = "fishing_trip_button",
                mascotResourceId = R.drawable.tilhoyre
            ),
            TutorialStep(
                title = "Da er du klar !",
                description = "Nå har du lært det grunnleggende i appen, og du er klar til å utforske norske farvann. God fisketur!",
                targetTag = "",
                mascotResourceId = R.drawable.presenting,
                isLastStep = true
            )
        )
    }
    
    // Observer tutorial tilstand for å oppdage når den er ferdig
    LaunchedEffect(tutorialManager.state.isCompleted) {
        if (tutorialManager.state.isCompleted) {
            // Varsle når tutorial er fullført
            onTutorialComplete()
        }
    }
    
    // Be om lokasjonstillatelse hvis nødvendig
    LaunchedEffect(Unit) {
        if (!locationPermissionState.value) {
            ActivityCompat.requestPermissions(
                context as android.app.Activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
    }

    // Gjenopprett fisketur-tilstand når appen starter
    LaunchedEffect(Unit) {
        val savedTripName = context.getSharedPreferences("fishing_trip", Context.MODE_PRIVATE)
            .getString("trip_name", "")
        val savedStartTime = context.getSharedPreferences("fishing_trip", Context.MODE_PRIVATE)
            .getLong("start_time", 0)
        val savedIsActive = context.getSharedPreferences("fishing_trip", Context.MODE_PRIVATE)
            .getBoolean("is_active", false)

        if (savedIsActive && savedStartTime > 0) {
            fishingTripName = savedTripName ?: ""
            isFishingTripActive = true
            fishingTripStartTime = LocalDateTime.ofEpochSecond(savedStartTime, 0, java.time.ZoneOffset.UTC)
        }
    }

    // Lagre fisketur-tilstand når den endres
    LaunchedEffect(isFishingTripActive, fishingTripStartTime, fishingTripName) {
        context.getSharedPreferences("fishing_trip", Context.MODE_PRIVATE).edit().apply {
            putString("trip_name", fishingTripName)
            putLong("start_time", fishingTripStartTime?.toEpochSecond(java.time.ZoneOffset.UTC) ?: 0)
            putBoolean("is_active", isFishingTripActive)
            apply()
        }
    }
    
    // Bruk en LaunchedEffect med isComingFromWelcome som key
    // Dette kjører kun når man faktisk kommer fra welcome screen
    LaunchedEffect(isComingFromWelcome) {
        if (isComingFromWelcome) {
            tutorialManager.startTutorial(tutorialSteps)
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
                    .tutorialTarget("search_button", tutorialManager),
                onSearch = { query ->
                    viewModel.searchAndMoveToLocation(query)
                }
            )
            
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
                            
                            // Sett opp async map loading med bedre error handling
                            try {
                                view.getMapAsync(OnMapReadyCallback { map ->
                                    try {
                                        Log.d(TAG, "Map is ready, setting up style and layers")
                                        mapLibreMap = map
                                        
                                        // Force-last inn standard style hvis det oppstår problemer
                                        val styleUrl = "https://api.maptiler.com/maps/streets-v2/style.json?key=oMZQoq4zniKOHeMvi7oA"
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
                                                    
                                                    // Håndter lokasjonsvalg først hvis vi er i lokasjonsvalg-modus
                                                    if (onLocationSelected != null) {
                                                        onLocationSelected(point.latitude, point.longitude, "Valgt lokasjon")
                                                        return@addOnMapClickListener true
                                                    }

                                                    // Check for fish log images first
                                                    val fishFeatures = map.queryRenderedFeatures(screenPoint)
                                                    val fishLog = fishFeatures.find { feature ->
                                                        val properties = feature.properties()
                                                        if (properties != null) {
                                                            val timestamp = properties.get("timestamp")?.asString
                                                            if (timestamp != null) {
                                                                fishLogUiState.fishLogs.find { it.timestamp.toString() == timestamp } != null
                                                            } else false
                                                        } else false
                                                    }?.let { feature ->
                                                        val properties = feature.properties()
                                                        val timestamp = properties?.get("timestamp")?.asString
                                                        if (timestamp != null) {
                                                            fishLogUiState.fishLogs.find { it.timestamp.toString() == timestamp }
                                                        } else null
                                                    }

                                                    if (fishLog != null) {
                                                        selectedLocation = Pair(fishLog.latitude, fishLog.longitude)
                                                        showFishLogDialog = true
                                                        resetSelections() // Nullstill GRIB-data og andre popups
                                                        return@addOnMapClickListener true
                                                    }
                                                    
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
                                                    
                                                    // Håndter fiskelogg posisjonsvalg før GRIB-data
                                                    if (showLocationSelectionDialog) {
                                                        selectedLocationForFish = Pair(point.latitude, point.longitude)
                                                        showLocationSelectionDialog = false
                                                        showAddFishDialog = true
                                                        selectedLocation = Pair(point.latitude, point.longitude)
                                                        return@addOnMapClickListener true
                                                    }
                                                    
                                                    // If no alert or ship was clicked, show GRIB data if enabled
                                                    if (showGrib && uiState.selectedAlert == null && !showFishLogDialog) {
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
                                                        return@addOnMapClickListener true
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
                                            } catch (e: Exception) {
                                                Log.e(TAG, "Error setting up map layers: ${e.message}")
                                                // Force en ny render om noe går galt
                                                map.triggerRepaint()
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error setting up map: ${e.message}")
                                    }
                                })
                            } catch (e: Exception) {
                                Log.e(TAG, "Error getting map async: ${e.message}")
                            }
                        }
                    },
                    update = { mapView ->
                        // Utfør oppdateringer til map view hvis nødvendig
                        if (mapLoadTimeout && mapLibreMap == null) {
                            // Forsøk å laste på nytt hvis timeout
                            try {
                                Log.d(TAG, "Attempting to reload map after timeout")
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
                
                // Vis loading-indikator hvis kart ikke er lastet
                if (mapLibreMap == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                // Vis reload-message hvis timeout har oppstått
                if (mapLoadTimeout) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Laster kartet...",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            // UI Elements
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Fisketur-knapp
                FloatingActionButton(
                    onClick = { showFishingTripDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 152.dp)
                        .tutorialTarget("fishing_trip_button", tutorialManager),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = "file:///android_asset/png/fisketur.png"
                        ),
                        contentDescription = "Start fisketur",
                        modifier = Modifier
                            .size(56.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                FloatingActionButton(
                    onClick = { 
                        showFishLogDialog = true
                        selectedLocation = null
                        selectedPoint = null
                        showPopup = false
                        showLocationSelectionDialog = false
                        showAddFishDialog = false
                        selectedLocationForFish = null
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 80.dp)
                        .tutorialTarget("fish_log_button", tutorialManager),
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
                    onDismiss = { showFilterMenu = false }
                )
            }

            if (showProfilePopup) {
                ProfilePopup(
                    userName = "$firstName $lastName",
                    onUserNameChange = { /* Handle name change */ },
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

            // Add Settings button
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 24.dp, end = 16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Settings button
                Surface(
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
                    failedImageLoads = failedImageLoads,
                    onImageLoadError = { imageId ->
                        failedImageLoads = failedImageLoads + imageId
                    },
                    onRemoveFish = { fishLog ->
                        // Fjern fisk fra loggen
                        fishLogViewModel.removeFishLog(fishLog)
                        
                        // Fjern fisk fra kartet
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
                        selectedPoint = null
                        showPopup = false
                    },
                    initialFishType = fishType,
                    initialLocation = location,
                    initialArea = area,
                    initialDescription = description,
                    initialWeight = weight,
                    initialImageUri = imageUri,
                    onFishTypeChange = { fishType = it },
                    onLocationChange = { location = it },
                    onAreaChange = { area = it },
                    onDescriptionChange = { description = it },
                    onWeightChange = { weight = it },
                    onImageUriChange = { imageUri = it },
                    selectedLocationForFish = selectedLocationForFish?.toString()
                )
            }

            // Vis fisketurdialog
            if (showFishingTripDialog) {
                FishingTripDialog(
                    onDismiss = { showFishingTripDialog = false },
                    tripName = fishingTripName,
                    onTripNameChange = { fishingTripName = it },
                    isTripActive = isFishingTripActive,
                    onStartTrip = { location ->
                        isFishingTripActive = true
                        fishingTripStartTime = LocalDateTime.now()
                        fishingTripStartLocation = LatLng(location.latitude, location.longitude)
                        fishingTripRoute = listOf(fishingTripStartLocation!!)
                        showFishingTripDialog = false
                    },
                    onEndTrip = {
                        isFishingTripActive = false
                        fishingTripEndTime = LocalDateTime.now()
                        
                        // Ta skjermbilde av kartet
                        mapLibreMap?.snapshot { bitmap ->
                            mapScreenshot = saveMapScreenshot(context, bitmap)
                        }
                        
                        showFishingTripDialog = false
                        showFishingTripSummary = true
                    },
                    startTime = fishingTripStartTime,
                    onClose = { showFishingTripDialog = false },
                    onAddCatch = { fishType, weight, uri, location ->
                        // Legg til fangst i fiskeloggen
                        val fishLog = FishLog(
                            fishType = fishType,
                            weight = try { weight.toFloat() } catch (e: Exception) { null },
                            timestamp = Date(),
                            latitude = location.latitude,
                            longitude = location.longitude,
                            area = "",
                            description = "",
                            imageUri = uri?.toString()
                        )
                        fishLogViewModel.addFishLog(fishLog)
                    },
                    fishLogViewModel = fishLogViewModel
                )
            }

            // Show fishing trip summary dialog
            if (showFishingTripSummary && fishingTripStartTime != null && fishingTripEndTime != null) {
                val catchesForTrip = fishLogUiState.fishLogs.filter { 
                    val logDate = it.timestamp
                    val startDate = Date.from(fishingTripStartTime!!.atZone(java.time.ZoneId.systemDefault()).toInstant())
                    val endDate = Date.from(fishingTripEndTime!!.atZone(java.time.ZoneId.systemDefault()).toInstant())
                    logDate.after(startDate) && logDate.before(endDate)
                }
                
                FishingTripSummaryDialog(
                    onDismiss = { 
                        // Lagre fisketuren i lagring når dialogen lukkes
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
                            
                            // Lagre turen i storage
                            FishingTripStorage.saveTrip(context, fishingTrip)
                        }
                        
                        showFishingTripSummary = false 
                    },
                    tripName = fishingTripName,
                    startTime = fishingTripStartTime!!,
                    endTime = fishingTripEndTime!!,
                    route = fishingTripRoute,
                    catches = catchesForTrip,
                    mapView = mapView,
                    mapScreenshot = mapScreenshot
                )
            }

            // Legg til tutorial overlay på toppen av alt
            TutorialOverlay(
                state = tutorialManager.state,
                onNext = { tutorialManager.nextStep() },
                onSkip = { tutorialManager.skipTutorial() }
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // Ryddig opprydding av mapView ressurser
            try {
                mapView?.onStop()
                mapView?.onDestroy()
                mapLibreMap = null
            } catch (e: Exception) {
                Log.e(TAG, "Error disposing map: ${e.message}")
            }
        }
    }
    
    // Legg til lifecycle-håndtering for MapView
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

// Funksjon for å lagre kartskjermbilde
private fun saveMapScreenshot(context: Context, bitmap: Bitmap): Uri? {
    return try {
        val fileName = "fishing_trip_${System.currentTimeMillis()}.jpg"
        val file = File(context.getExternalFilesDir(null), fileName)
        
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        
        Uri.fromFile(file)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to save map screenshot: ${e.message}")
        null
    }
} 