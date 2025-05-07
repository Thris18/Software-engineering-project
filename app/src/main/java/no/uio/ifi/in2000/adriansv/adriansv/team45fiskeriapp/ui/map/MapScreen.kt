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
import org.maplibre.android.style.layers.PropertyFactory.*
import android.net.Uri
import androidx.compose.foundation.Image
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fish.FishLogViewModel
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fish.FishLogDialog
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fish.AddFishDialog
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.fish.FishLog
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.R
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.tutorial.*
import kotlinx.coroutines.delay
import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.graphics.Bitmap
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.time.LocalDateTime
import java.util.*
import android.os.Build
import androidx.annotation.RequiresApi
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fishing.FishingTrip
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fishing.FishingTripStorage
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fishing.FishingTripDialog
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fishing.FishingTripSummaryDialog
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.weather.WeatherScreen
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import java.io.File
import java.io.FileOutputStream
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib.GribOverlayManager
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib.GribOverlayUtil

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

// Hjelpefunksjon for å rotere bitmap basert på EXIF-orientering
private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
    val matrix = android.graphics.Matrix()
    when (orientation) {
        android.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        android.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        android.media.ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        android.media.ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
        android.media.ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
        android.media.ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.postRotate(90f)
            matrix.postScale(-1f, 1f)
        }
        android.media.ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.postRotate(90f)
            matrix.postScale(1f, -1f)
        }
        else -> return bitmap
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
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

    // State for GRIB overlay loading
    var gribOverlayLoading by remember { mutableStateOf(false) }

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

    // Observer for søkeresultat og oppdater kameraposisjon
    val searchTarget by viewModel.searchTarget.collectAsStateWithLifecycle()

    LaunchedEffect(searchTarget) {
        searchTarget?.let { target ->
            val zoom = 12.0
            Log.d(TAG, "Flytter kamera til søkeresultat: ${target.latitude}, ${target.longitude}")
            mapLibreMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(target.latitude, target.longitude),
                zoom
            ))
            weatherViewModel.updateWeather(target.latitude, target.longitude, zoom)
        }
    }

    // Oppdater værvarsel når kartet er lastet
    LaunchedEffect(mapLibreMap) {
        mapLibreMap?.getStyle { style ->
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
                // Håndter EXIF-rotasjon for filer
                val exif = android.media.ExifInterface(uri.path!!)
                val orientation = exif.getAttributeInt(
                    android.media.ExifInterface.TAG_ORIENTATION,
                    android.media.ExifInterface.ORIENTATION_NORMAL
                )
                val bitmap = BitmapFactory.decodeFile(uri.path)
                rotateBitmap(bitmap, orientation)
            } else {
                // Håndter EXIF-rotasjon for content URIs
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val exif = android.media.ExifInterface(stream)
                    val orientation = exif.getAttributeInt(
                        android.media.ExifInterface.TAG_ORIENTATION,
                        android.media.ExifInterface.ORIENTATION_NORMAL
                    )
                    stream.reset() // Reset stream for bitmap decoding
                    val bitmap = BitmapFactory.decodeStream(stream)
                    rotateBitmap(bitmap, orientation)
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

                    // Bestem størrelsen basert på om bildet er fra en fisketur eller fiskelogg
                    val iconSize = if (fishLog.area == fishingTripName) {
                        0.40f  // Justert størrelse for fisketur-bilder
                    } else {
                        0.05f // Behold original størrelse for fiskelogg-bilder
                    }

                    // Legg til symbol layer
                    val layer = SymbolLayer("fish_${fishLog.timestamp}", "fish_${fishLog.timestamp}")
                        .withProperties(
                            iconImage(imageId),
                            iconSize(iconSize),
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

    // Observer tutorial tilstand for å oppdage når den er ferdig
    LaunchedEffect(tutorialManager.isCompleted) {
        if (tutorialManager.isCompleted) {
            // Varsle når tutorial er fullført
            onTutorialComplete()
        }
    }

    // Bruk en LaunchedEffect med isComingFromWelcome som key
    // Dette kjører kun når man faktisk kommer fra welcome screen
    LaunchedEffect(isComingFromWelcome) {
        if (isComingFromWelcome) {
            tutorialManager.startTutorial()
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

    // Oppdater brukerens posisjon
    LaunchedEffect(mapLibreMap) {
        mapLibreMap?.getStyle { style ->
            try {
                // Legg til kilde for brukerens posisjon
                val userLocationSource = GeoJsonSource("user-location-source")
                style.addSource(userLocationSource)

                // Legg til kilde for fisketur-ruten
                val routeSource = GeoJsonSource("fishing-trip-route-source")
                style.addSource(routeSource)

                // Legg til lag for fisketur-ruten (gul linje)
                val routeLayer = LineLayer("fishing-trip-route-layer", "fishing-trip-route-source")
                    .withProperties(
                        lineColor(Color.YELLOW),
                        lineWidth(4f),
                        lineOpacity(0.8f)
                    )
                style.addLayer(routeLayer)

                // Legg til lag for brukerens posisjon (blå sirkel)
                val userLocationLayer = CircleLayer("user-location-layer", "user-location-source")
                    .withProperties(
                        circleRadius(8f),
                        circleColor(Color.BLUE),
                        circleOpacity(0.9f),
                        circleStrokeWidth(2f),
                        circleStrokeColor(Color.WHITE)
                    )
                style.addLayer(userLocationLayer)

                Log.d(TAG, "Location layers and sources added to map")

                // Start oppdatering av brukerens posisjon
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (locationPermissionState.value) {
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Log.d(TAG, "Starting location updates")
                        locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            1000L, // Oppdater hvert sekund
                            1f // Oppdater hvis brukeren beveger seg mer enn 1 meter
                        ) { location ->
                            Log.d(TAG, "Location update received: lat=${location.latitude}, lon=${location.longitude}, accuracy=${location.accuracy}")
                            
                            // Oppdater brukerens posisjon
                            userLocation = LatLng(location.latitude, location.longitude)
                            
                            // Oppdater GeoJSON-kilden med brukerens posisjon
                            val geoJson = """
                            {
                                "type": "Feature",
                                "geometry": {
                                    "type": "Point",
                                    "coordinates": [${location.longitude}, ${location.latitude}]
                                }
                            }
                            """
                            try {
                                userLocationSource.setGeoJson(geoJson)
                                Log.d(TAG, "Successfully updated user location on map")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error updating user location on map: ${e.message}")
                            }

                            // Hvis fisketuren er aktiv, legg til posisjonen i ruten
                            if (isFishingTripActive) {
                                Log.d(TAG, "Fishing trip is active, updating route")
                                // Sjekk om den nye posisjonen er forskjellig fra den siste i ruten
                                val lastPosition = fishingTripRoute.lastOrNull()
                                val newPosition = LatLng(location.latitude, location.longitude)

                                if (lastPosition == null ||
                                    (lastPosition.latitude != newPosition.latitude ||
                                     lastPosition.longitude != newPosition.longitude)) {
                                    fishingTripRoute = fishingTripRoute + newPosition
                                    Log.d(TAG, "Updated fishing trip route. New route size: ${fishingTripRoute.size}")

                                    // Oppdater ruten på kartet
                                    val routeGeoJson = """
                                    {
                                        "type": "Feature",
                                        "geometry": {
                                            "type": "LineString",
                                            "coordinates": [${fishingTripRoute.joinToString(",") { "[${it.longitude}, ${it.latitude}]" }}]
                                        }
                                    }
                                    """
                                    try {
                                        routeSource.setGeoJson(routeGeoJson)
                                        Log.d(TAG, "Successfully updated route on map")
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error updating route on map: ${e.message}")
                                    }
                                }
                            }

                            // Hvis vi følger brukeren, oppdater kameraet
                            if (isTrackingUser) {
                                mapLibreMap?.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(userLocation!!, 18.0)
                                )
                            }
                        }
                    } else {
                        Log.e(TAG, "GPS provider is not enabled")
                    }
                } else {
                    Log.e(TAG, "Location permission not granted")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting up location tracking: ${e.message}")
            }
        }
    }

    // Oppdater isTrackingUser når fisketur starter
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

                                        // Velg stil basert på dark mode
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
                        .padding(bottom = 143.dp)
                        .tutorialTarget("fishing_trip_button", tutorialManager),
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

            // I MapScreen, etter at selectedPoint settes og showPopup = true, vis overlay:
            LaunchedEffect(mapLibreMap) {
                if (mapLibreMap != null) {
                    gribOverlayLoading = true
                    val allGrids = gribRepository.getAllWeatherGrids()
                    val geoJson = GribOverlayUtil.mergeFeatureCollections(
                        allGrids["wind"]?.let { GribOverlayUtil.gribDataToFeatureCollection(it, "wind", "wind") } ?: "",
                        allGrids["wave"]?.let { GribOverlayUtil.gribDataToFeatureCollection(it, "wave", "wave") } ?: "",
                        allGrids["strom"]?.let { GribOverlayUtil.gribDataToFeatureCollection(it, "strom", "strom") } ?: "",
                        allGrids["rain"]?.let { GribOverlayUtil.gribDataToFeatureCollection(it, "rain", "rain") } ?: ""
                    )
                    mapLibreMap!!.getStyle { style ->
                        Log.d("GRIB", "GeoJSON: $geoJson")
                        GribOverlayManager.addOrUpdateGribOverlay(context, style, geoJson)
                        gribOverlayLoading = false
                    }
                }
            }

            // Show fishing trip dialog
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
                        // Stopp tracking av brukerens posisjon
                        isTrackingUser = false
                        
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
                            area = fishingTripName, // Bruk fisketur-navn som område
                            description = "Fanget under fisketur: $fishingTripName",
                            imageUri = uri?.toString()
                        )
                        fishLogViewModel.addFishLog(fishLog)

                        // Oppdater kartet med den nye fangsten
                        mapLibreMap?.getStyle { style ->
                            if (fishLog.imageUri != null) {
                                loadImage(style, fishLog)
                            }
                        }
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
                state = tutorialManager.tutorialState,
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

    // I NavigationRail, endre WeatherScreen-kallet
    if (currentRoute == "weather") {
        WeatherScreen(
            weather = weatherUiState.weather,
            viewModel = weatherViewModel,
            mapCenter = mapLibreMap?.cameraPosition?.target
        )
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