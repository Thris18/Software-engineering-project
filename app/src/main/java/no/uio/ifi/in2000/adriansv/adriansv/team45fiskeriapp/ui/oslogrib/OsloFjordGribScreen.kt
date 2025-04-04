package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.oslogrib

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OslofjordGribScreen(
    viewModel: OslofjordGribViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalContext.current as LifecycleOwner
    var mapView: MapView? by remember { mutableStateOf(null) }
    var showDataPanel by remember { mutableStateOf(false) }
    var hasSelectedPoint by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        mapView = MapView(context).apply {
            // Konfigurerer kartet
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)

            // Setter initiell posisjon til Oslofjorden litt zoomet inn
            controller.setZoom(10.5)
            controller.setCenter(GeoPoint(59.7, 10.65))

            // Setter zoom limits
            minZoomLevel = 8.0
            maxZoomLevel = 19.0

//            // Restrict panning to Oslofjord region
//            setScrollableAreaLimitDouble(
//                BoundingBox(
//                    60.2, // North
//                    11.2, // East
//                    59.0, // South
//                    10.0  // West
//                )
//            )
        }

        // Laster alle datatyper automatisk
        viewModel.loadAllGribData(context)
    }

    // Håndterer lifecycle til kartet
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    mapView?.onResume()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    mapView?.onPause()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Oppdaterer map overlays basert på GRIB-data
    LaunchedEffect(uiState.gribData) {
        val currentGribData = uiState.gribData ?: return@LaunchedEffect

        mapView?.let { map ->
            // Clear eksisterende data overlays
            val markersToRemove = map.overlays.filterIsInstance<Marker>()
            map.overlays.removeAll(markersToRemove)

            try {
                // Adder map click event som håndterer klikk på kartet
                val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                        p?.let {
                            viewModel.getGribDataAtCoordinates(it.latitude, it.longitude)
                            hasSelectedPoint = true
                            // Ikke vis panel automatisk lenger når man trykker på kartet
                        }
                        return true
                    }

                    override fun longPressHelper(p: GeoPoint?): Boolean {
                        return false
                    }
                })

                map.overlays.add(mapEventsOverlay)

                // Hvis vi har et valgt punkt, legg til en highlight markør
                uiState.selectedPoint?.let { selectedPoint ->
                    val marker = Marker(map)
                    marker.position = GeoPoint(selectedPoint.latitude, selectedPoint.longitude)
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = "${selectedPoint.variableName}: ${selectedPoint.value.format(2)} ${selectedPoint.unit}"
                    marker.snippet = "Data point"
                    map.overlays.add(marker)
                }

                map.invalidate()
            } catch (e: Exception) {
                Log.e("OslofjordGribScreen", "Error creating data visualization: ${e.message}", e)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Map view
        mapView?.let { view ->
            AndroidView(
                factory = { view },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Info knapp i øverste høyre hjørne
        FloatingActionButton(
            onClick = {
                // Hvis panel hvis en punkt blir valgt/trykket på
                if (hasSelectedPoint && uiState.allPoints.isNotEmpty()) {
                    showDataPanel = true
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(56.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Show Information"
            )
        }

        // Loading indicator
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Valgt datapunkt display med flere datatyper
        if (showDataPanel && uiState.allPoints.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Header row med tittel og close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Weather Data",
                            style = MaterialTheme.typography.titleMedium
                        )

                        IconButton(
                            onClick = { showDataPanel = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close"
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Viser alle datatyper i en spesifikk rekkefølge
                    val orderedTypes = listOf("nedbør", "strøm", "vind", "bølgehøyde")

                    orderedTypes.forEach { type ->
                        val point = uiState.allPoints[type]
                        if (point != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = point.variableName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "${point.value.format(2)} ${point.unit}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Vis lokasjonsinfo
                    val location = uiState.selectedPoint
                    if (location != null) {
                        Text(
                            text = "Location: ${location.latitude.format(4)}, ${location.longitude.format(4)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

// Hjelpefunksjon for formatering
private fun Float.format(digits: Int): String {
    return "%.${digits}f".format(this)
}

private fun Double.format(digits: Int): String {
    return "%.${digits}f".format(this)
}