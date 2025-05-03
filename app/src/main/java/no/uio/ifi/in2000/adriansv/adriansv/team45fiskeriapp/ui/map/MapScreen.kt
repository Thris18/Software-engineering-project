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
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.R
import androidx.compose.ui.graphics.ColorFilter
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components.NavigationBar
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather.LocationWeather
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fishing.FishingTripDialog
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.weather.WeatherUiState
import java.time.LocalDateTime
import java.util.*
import android.location.Location
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fish.FishLogViewModel
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fish.FishLogDialog
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fish.AddFishDialog
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.fish.FishLog
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fishing.FishingTripSummaryDialog
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.LineLayer
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fishing.FishingTrip
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fishing.FishingTripStorage

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
    onLocationSelected: ((Double, Double, String) -> Unit)? = null
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

    // Oppdater fiskelogg-bilder på kartet når fiskeloggen endres
    LaunchedEffect(fishLogUiState.fishLogs) {
        mapLibreMap?.getStyle { style ->
            try {
                // Fjern eksisterende fiskelag og kilde
                style.getLayer("fish-layer")?.let { style.removeLayer(it) }
                style.getSource("fish-source")?.let { style.removeSource(it) }
                
                // Last inn alle fiskebilder med en gang
            fishLogUiState.fishLogs.forEach { fishLog ->
                if (fishLog.imageUri != null) {
                        try {
                            val imageId = "fish_${fishLog.timestamp}"
                            context.contentResolver.openInputStream(Uri.parse(fishLog.imageUri))?.use { inputStream ->
                                val image = BitmapFactory.decodeStream(inputStream)
                                if (image != null) {
                                    // Skaler bildet til en fast høyde mens vi beholder bildets form
                                    val targetHeight = 200
                                    val aspectRatio = image.width.toFloat() / image.height.toFloat()
                                    val targetWidth = (targetHeight * aspectRatio).toInt()
                                    val scaledImage = android.graphics.Bitmap.createScaledBitmap(image, targetWidth, targetHeight, true)
                                    style.addImage(imageId, scaledImage)
                                    loadedImages = loadedImages + imageId
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Feil ved lasting av bilde: ${e.message}")
                            failedImageLoads = failedImageLoads + fishLog.imageUri
                        }
                    }
                }
                
                // Opprett GeoJSON for alle fisker
                val features = fishLogUiState.fishLogs.map { fishLog ->
                    """
                    {
                        "type": "Feature",
                        "geometry": {
                            "type": "Point",
                            "coordinates": [${fishLog.longitude}, ${fishLog.latitude}]
                        },
                        "properties": {
                            "timestamp": "${fishLog.timestamp}",
                            "imageUri": "fish_${fishLog.timestamp}"
                        }
                    }
                    """
                }.joinToString(",")
                
                val geoJson = """
                {
                    "type": "FeatureCollection",
                    "features": [$features]
                }
                """
                
                // Legg til kilde og lag for fisker
                val fishSource = GeoJsonSource("fish-source", geoJson)
                style.addSource(fishSource)
                
                val layer = SymbolLayer("fish-layer", "fish-source")
                    .withProperties(
                        iconImage(
                            Expression.coalesce(
                                Expression.get("imageUri"),
                                Expression.literal("fish_icon")
                            )
                        ),
                        iconSize(0.3f),
                        iconAllowOverlap(true),
                        iconIgnorePlacement(true),
                        iconAnchor(Property.ICON_ANCHOR_CENTER),
                        iconOpacity(1.0f)
                    )
                style.addLayer(layer)
            } catch (e: Exception) {
                Log.e(TAG, "Feil ved oppdatering av fiskelag: ${e.message}")
            }
        }
    }

    // Håndter lokasjonsvalg
    LaunchedEffect(showLocationSelectionDialog) {
        if (showLocationSelectionDialog) {
            showAddFishDialog = false
        }
    }

    Team45FiskeriAppTheme(darkTheme = isDarkTheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Søkeknapp plassert øverst på skjermen
            SokeKnapp(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .zIndex(1f),
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

                                    // Last inn alle fiskebilder med en gang
                                    fishLogUiState.fishLogs.forEach { fishLog ->
                                        if (fishLog.imageUri != null) {
                                            try {
                                                val imageId = "fish_${fishLog.timestamp}"
                                                context.contentResolver.openInputStream(Uri.parse(fishLog.imageUri))?.use { inputStream ->
                                                    val image = BitmapFactory.decodeStream(inputStream)
                                                    if (image != null) {
                                                        // Skaler bildet til en fast høyde mens vi beholder bildets form
                                                        val targetHeight = 200
                                                        val aspectRatio = image.width.toFloat() / image.height.toFloat()
                                                        val targetWidth = (targetHeight * aspectRatio).toInt()
                                                        val scaledImage = android.graphics.Bitmap.createScaledBitmap(image, targetWidth, targetHeight, true)
                                                        style.addImage(imageId, scaledImage)
                                                        loadedImages = loadedImages + imageId
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                Log.e(TAG, "Feil ved lasting av bilde: ${e.message}")
                                                failedImageLoads = failedImageLoads + fishLog.imageUri
                                            }
                                        }
                                    }

                                    // Opprett GeoJSON for alle fisker
                                    val features = fishLogUiState.fishLogs.map { fishLog ->
                                        """
                                        {
                                            "type": "Feature",
                                            "geometry": {
                                                "type": "Point",
                                                "coordinates": [${fishLog.longitude}, ${fishLog.latitude}]
                                            },
                                            "properties": {
                                                "timestamp": "${fishLog.timestamp}",
                                                "imageUri": "fish_${fishLog.timestamp}"
                                            }
                                        }
                                        """
                                    }.joinToString(",")
                                    
                                    val geoJson = """
                                    {
                                        "type": "FeatureCollection",
                                        "features": [$features]
                                    }
                                    """
                                    
                                    // Legg til kilde og lag for fisker
                                    val fishSource = GeoJsonSource("fish-source", geoJson)
                                    style.addSource(fishSource)
                                    
                                    val layer = SymbolLayer("fish-layer", "fish-source")
                                        .withProperties(
                                            iconImage(
                                                Expression.coalesce(
                                                    Expression.get("imageUri"),
                                                    Expression.literal("fish_icon")
                                                )
                                            ),
                                            iconSize(0.3f),
                                            iconAllowOverlap(true),
                                            iconIgnorePlacement(true),
                                            iconAnchor(Property.ICON_ANCHOR_CENTER),
                                            iconOpacity(1.0f)
                                        )
                                    style.addLayer(layer)

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
                                    val alertSource = GeoJsonSource("alerts-source", uiState.geoJsonData)
                                    style.addSource(alertSource)

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
                                }
                            })
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
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
                        .padding(bottom = 152.dp),
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

                // Fiskelogg button
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
                        .padding(bottom = 80.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.fiskelogg),
                        contentDescription = "Fiskelogg",
                        modifier = Modifier
                            .size(48.dp)
                            .padding(4.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                // Båtvett button
                BaatvettButton(
                    onClick = { showBaatvettRules = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 16.dp)
                )
            }
            

            // Popups and overlays
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

            // Show fishing trip dialog
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
                        userLocation?.let { userLoc ->
                            mapLibreMap?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(userLoc, 18.0)
                            )
                        }
                    },
                    onEndTrip = {
                        isFishingTripActive = false
                        fishingTripEndTime = LocalDateTime.now()
                        fishingTripStartLocation = null
                        isTrackingUser = false
                        
                        // Ta snapshot av kartet slik det ser ut nå, uten å flytte kameraet
                        mapView?.let { view ->
                            try {
                                if (fishingTripRoute.isNotEmpty() && fishingTripRoute.any { 
                                    !it.latitude.isNaN() && !it.longitude.isNaN() 
                                }) {
                                    mapLibreMap?.snapshot { bitmap ->
                                        if (bitmap != null) {
                                            // Lagre nytt screenshot
                                            val filename = "fishing_trip_${System.currentTimeMillis()}.jpg"
                                            val file = File(context.filesDir, filename)
                                            FileOutputStream(file).use { out ->
                                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                                            }
                                            mapScreenshot = Uri.fromFile(file)
                                            Log.d(TAG, "Screenshot lagret til: ${file.absolutePath}")

                                            // LAGRE FISKETUR
                                            val tripName = fishingTripName
                                            val screenshotUri = mapScreenshot?.toString() ?: ""
                                            val startMillis = fishingTripStartTime?.atZone(java.time.ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: 0L
                                            val endMillis = fishingTripEndTime?.atZone(java.time.ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: 0L
                                            val catchCount = fishLogUiState.fishLogs.count { it.area == tripName }
                                            FishingTripStorage.saveTrip(
                                                context,
                                                FishingTrip(
                                                    name = tripName,
                                                    screenshotUri = screenshotUri,
                                                    startTime = startMillis,
                                                    endTime = endMillis,
                                                    catchCount = catchCount
                                                )
                                            )
                                        } else {
                                            Log.e(TAG, "Kunne ikke ta screenshot av kartet")
                                        }
                                        showFishingTripSummary = true
                                    }
                                } else {
                                    Log.d(TAG, "Ingen gyldig rute å vise")
                                    showFishingTripSummary = true
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Feil ved snapshot av kartet: ${e.message}")
                                showFishingTripSummary = true
                            }
                        } ?: run {
                            Log.d(TAG, "mapView er null")
                            showFishingTripSummary = true
                        }
                    },
                    startTime = fishingTripStartTime,
                    onClose = { showFishingTripDialog = false },
                    onAddCatch = { fishType: String, weight: Double, imageUri: Uri?, location: Location ->
                        try {
                            // Opprett FishLog med riktig posisjon
                            val fishLog = FishLog(
                                fishType = fishType,
                                area = fishingTripName,
                                weight = weight.toFloat(),
                                imageUri = imageUri?.toString(),
                                latitude = location.latitude,
                                longitude = location.longitude,
                                timestamp = Date()
                            )
                            
                            // Legg til fangsten i repository
                            fishLogViewModel.addFishLog(fishLog)
                            
                            // Kartet vil automatisk oppdateres gjennom LaunchedEffect
                        } catch (e: Exception) {
                            Log.e(TAG, "Feil ved lagring av fangst: ${e.message}")
                        }
                    },
                    fishLogViewModel = fishLogViewModel
                )
            }

            // Vis oppsummeringsdialog når fisketuren er avsluttet
            if (showFishingTripSummary && fishingTripStartTime != null && fishingTripEndTime != null) {
                FishingTripSummaryDialog(
                    onDismiss = { 
                        showFishingTripSummary = false
                        fishingTripRoute = emptyList()
                        fishingTripStartTime = null
                        fishingTripEndTime = null
                        mapScreenshot = null
                    },
                    tripName = fishingTripName,
                    startTime = fishingTripStartTime!!,
                    endTime = fishingTripEndTime!!,
                    route = fishingTripRoute,
                    catches = fishLogUiState.fishLogs.filter { it.area == fishingTripName },
                    mapView = mapView,
                    mapScreenshot = mapScreenshot
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mapView?.onDestroy()
        }
    }

    // Oppdater brukerens posisjon
    LaunchedEffect(mapLibreMap) {
        mapLibreMap?.getStyle { style ->
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
            
            // Start oppdatering av brukerens posisjon
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (locationPermissionState.value) {
                try {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        1000L, // Oppdater hvert sekund
                        1f // Oppdater hvis brukeren beveger seg mer enn 1 meter
                    ) { location ->
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
                        userLocationSource.setGeoJson(geoJson)
                        
                        // Hvis fisketuren er aktiv, legg til posisjonen i ruten
                        if (isFishingTripActive) {
                            // Sjekk om den nye posisjonen er forskjellig fra den siste i ruten
                            val lastPosition = fishingTripRoute.lastOrNull()
                            val newPosition = LatLng(location.latitude, location.longitude)
                            
                            if (lastPosition == null || 
                                (lastPosition.latitude != newPosition.latitude || 
                                 lastPosition.longitude != newPosition.longitude)) {
                                fishingTripRoute = fishingTripRoute + newPosition
                                
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
                                routeSource.setGeoJson(routeGeoJson)
                            }
                        }
                        
                        // Hvis vi følger brukeren, oppdater kameraet
                        if (isTrackingUser) {
                            mapLibreMap?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(userLocation!!, 18.0)
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Feil ved oppdatering av brukerens posisjon: ${e.message}")
                }
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