package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fishing

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import android.location.Location
import android.location.LocationManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import android.content.Intent
import android.provider.MediaStore
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.fish.FishLogRepository
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.fish.FishLog
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fish.FishLogViewModel
import java.util.Date
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FishingTripDialog(
    onDismiss: () -> Unit,
    tripName: String,
    onTripNameChange: (String) -> Unit,
    isTripActive: Boolean,
    onStartTrip: (Location) -> Unit,
    onEndTrip: () -> Unit,
    startTime: LocalDateTime?,
    onClose: () -> Unit,
    onAddCatch: (String, Double, Uri?, Location) -> Unit,
    fishLogViewModel: FishLogViewModel
) {
    var showDialog by remember { mutableStateOf(true) }
    var currentDuration by remember { mutableStateOf<Duration?>(null) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showLocationPermissionDialog by remember { mutableStateOf(false) }
    var showLocationErrorDialog by remember { mutableStateOf(false) }
    var showLocationServiceDialog by remember { mutableStateOf(false) }
    var isGettingLocation by remember { mutableStateOf(false) }
    var shouldGetLocation by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var showAddCatchDialog by remember { mutableStateOf(false) }
    var showEndTripConfirmation by remember { mutableStateOf(false) }
    var selectedFishType by remember { mutableStateOf("") }
    var selectedWeight by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedDescription by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf<Location?>(null) }
    var showCameraPermissionDialog by remember { mutableStateOf(false) }
    var showCatches by remember { mutableStateOf(false) }
    var catches by remember { mutableStateOf<List<Triple<String, Double, Uri?>>>(emptyList()) }
    
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationManager = remember { context.getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    val fishLogRepository = remember { FishLogRepository(context) }
    val fishLogs by fishLogRepository.fishLogs.collectAsState(initial = emptyList())
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val photo = result.data?.extras?.get("data") as? android.graphics.Bitmap
            photo?.let {
                // Konverter bitmap til URI og lagre
                val uri = saveImageToInternalStorage(context, it)
                selectedImageUri = uri
            }
        }
    }
    
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Start kamera
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(intent)
        } else {
            showCameraPermissionDialog = true
        }
    }
    
    // Oppdater posisjon kontinuerlig når turen er aktiv
    LaunchedEffect(isTripActive) {
        if (isTripActive) {
            try {
                val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                    .setMinUpdateIntervalMillis(3000)
                    .build()
                
                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        locationResult.lastLocation?.let { location ->
                            currentLocation = location
                        }
                    }
                }
                
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        null
                    )
                }
            } catch (e: Exception) {
                showLocationErrorDialog = true
            }
        }
    }
    
    suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { continuation ->
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    continuation.resume(location)
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }
    
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                shouldGetLocation = true
            } else {
                showLocationServiceDialog = true
            }
        } else {
            showLocationPermissionDialog = true
        }
    }
    
    // Håndter posisjonshenting
    LaunchedEffect(shouldGetLocation) {
        if (shouldGetLocation) {
            isGettingLocation = true
            try {
                val location = getCurrentLocation()
                if (location != null) {
                    currentLocation = location
                    onStartTrip(location)
                } else {
                    showLocationErrorDialog = true
                }
            } catch (e: Exception) {
                showLocationErrorDialog = true
            } finally {
                isGettingLocation = false
                shouldGetLocation = false
            }
        }
    }
    
    // Oppdater tiden hvert sekund når turen er aktiv
    LaunchedEffect(isTripActive, startTime) {
        while (isTripActive && startTime != null) {
            currentDuration = Duration.between(startTime, LocalDateTime.now())
            delay(1000)
        }
    }
    
    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Text(
                        text = if (isTripActive) tripName else "Start din fisketur",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    if (!isTripActive) {
                        // Start tur visning
                        OutlinedTextField(
                            value = tripName,
                            onValueChange = onTripNameChange,
                            label = { Text("Navn på fisketur") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = MaterialTheme.shapes.medium
                        )

                        Button(
                            onClick = {
                                when {
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED -> {
                                        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                            shouldGetLocation = true
                                        } else {
                                            showLocationServiceDialog = true
                                        }
                                    }
                                    else -> {
                                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            enabled = tripName.isNotBlank() && !isGettingLocation,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            if (isGettingLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(
                                    "Start fisketur",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        // Aktiv tur visning
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Varighet
                                currentDuration?.let { duration ->
                                    val hours = duration.toHours()
                                    val minutes = duration.toMinutesPart()
                                    val seconds = duration.toSecondsPart()
                                    
                                    Text(
                                        text = "Varighet",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = String.format("%02d:%02d:%02d", hours, minutes, seconds),
                                        style = MaterialTheme.typography.headlineLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                }
                                
                                Divider(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                                
                                // Posisjon
                                currentLocation?.let { location ->
                                    Text(
                                        text = "Din posisjon",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "Latitude",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                            Text(
                                                text = String.format("%.6f", location.latitude),
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "Longitude",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                            Text(
                                                text = String.format("%.6f", location.longitude),
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Knapper for aktiv tur
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Legg til fangst knapp
                            Button(
                                onClick = { showAddCatchDialog = true },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(40.dp)
                                    .padding(end = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(
                                    "Legg til fangst",
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1
                                )
                            }

                            // Avslutt tur knapp
                            Button(
                                onClick = { showConfirmationDialog = true },
                                modifier = Modifier
                                    .weight(0.8f)
                                    .height(40.dp)
                                    .padding(start = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                )
                            ) {
                                Text(
                                    "Avslutt tur",
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Vis fangst seksjon
                    if (fishLogs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        if (isTripActive) {
                            Button(
                                onClick = { showCatches = !showCatches },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        if (showCatches) "Skjul fangst" else "Vis fangst",
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = if (showCatches) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (showCatches) "Skjul fangst" else "Vis fangst",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        if (showCatches) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .heightIn(max = 300.dp)
                            ) {
                                items(fishLogs.filter { it.area == tripName }) { fishLog ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp)
                                        ) {
                                            Text(
                                                text = fishLog.fishType,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                text = "Vekt: ${fishLog.weight}kg",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            if (fishLog.description.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = fishLog.description,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                )
                                            }
                                            fishLog.imageUri?.let { uri ->
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Image(
                                                    painter = rememberAsyncImagePainter(uri),
                                                    contentDescription = "Fangst bilde",
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(100.dp)
                                                        .clip(RoundedCornerShape(8.dp)),
                                                    contentScale = ContentScale.Fit
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Posisjon: ${String.format("%.4f°N, %.4f°Ø", fishLog.latitude, fishLog.longitude)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Lukk knapp
                TextButton(
                        onClick = { 
                            showDialog = false
                            onClose()
                        },
                        modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Lukk")
                    }
                }
            }
        }
    }

    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text("Avslutt fisketur") },
            text = { Text("Er du sikker på at du vil avslutte fisketuren?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmationDialog = false
                        onEndTrip()
                        showDialog = false
                        showDialog = true
                    }
                ) {
                    Text("Ja, avslutt")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmationDialog = false }
                ) {
                    Text("Nei, fortsett")
                }
            }
        )
    }

    if (showLocationPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showLocationPermissionDialog = false },
            title = { Text("Posisjonstillatelse kreves") },
            text = { Text("For å registrere fisketuren trenger vi tilgang til din posisjon. Vennligst gi tillatelse i innstillingene.") },
            confirmButton = {
                TextButton(
                    onClick = { showLocationPermissionDialog = false }
                ) {
                    Text("OK")
                }
            }
        )
    }

    if (showLocationServiceDialog) {
        AlertDialog(
            onDismissRequest = { showLocationServiceDialog = false },
            title = { Text("GPS er ikke aktivert") },
            text = { Text("For å registrere fisketuren trenger vi tilgang til din posisjon. Vennligst skru på GPS i innstillingene.") },
            confirmButton = {
                TextButton(
                    onClick = { 
                        showLocationServiceDialog = false
                        val intent = android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        context.startActivity(intent)
                    }
                ) {
                    Text("Åpne innstillinger")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLocationServiceDialog = false }
                ) {
                    Text("Avbryt")
                }
            }
        )
    }

    if (showLocationErrorDialog) {
        AlertDialog(
            onDismissRequest = { showLocationErrorDialog = false },
            title = { Text("Kunne ikke hente posisjon") },
            text = { Text("Vi kunne ikke hente din posisjon. Vennligst sjekk at GPS er skrudd på og prøv igjen.") },
            confirmButton = {
                TextButton(
                    onClick = { showLocationErrorDialog = false }
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Legg til fangst dialog
    if (showAddCatchDialog) {
        AlertDialog(
            onDismissRequest = { showAddCatchDialog = false },
            title = { Text("Legg til fangst") },
            text = {
                Column {
                    OutlinedTextField(
                        value = selectedFishType,
                        onValueChange = { selectedFishType = it },
                        label = { Text("Fisketype") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = selectedWeight,
                        onValueChange = { newValue ->
                            // Tillat kun tall og punktum
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                selectedWeight = newValue
                            }
                        },
                        label = { Text("Vekt (kg)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = selectedDescription,
                        onValueChange = { selectedDescription = it },
                        label = { Text("Beskrivelse") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ta bilde")
                    }
                    selectedImageUri?.let { uri ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Tatt bilde",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val weight = selectedWeight.toDoubleOrNull() ?: 0.0
                        currentLocation?.let { location ->
                            // Opprett FishLog med beskrivelse
                            val fishLog = FishLog(
                                fishType = selectedFishType,
                                area = tripName,
                                description = selectedDescription,
                                weight = weight.toFloat(),
                                imageUri = selectedImageUri?.toString(),
                                latitude = location.latitude,
                                longitude = location.longitude,
                                timestamp = Date()
                            )
                            
                            // Legg til fangsten i repository
                            fishLogViewModel.addFishLog(fishLog)
                            
                            // Oppdater fangstlisten umiddelbart
                            showCatches = true
                            
                            showAddCatchDialog = false
                            selectedFishType = ""
                            selectedWeight = ""
                            selectedDescription = ""
                            selectedImageUri = null
                        }
                    },
                    enabled = selectedFishType.isNotBlank() && selectedWeight.isNotBlank() && currentLocation != null
                ) {
                    Text("Legg til")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCatchDialog = false }) {
                    Text("Avbryt")
                }
            }
        )
    }

    if (showCameraPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showCameraPermissionDialog = false },
            title = { Text("Kameratillatelse kreves") },
            text = { Text("For å ta bilde av fangsten trenger appen tilgang til kameraet. Vennligst gi tilgang i innstillingene.") },
            confirmButton = {
                TextButton(onClick = { showCameraPermissionDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

private fun saveImageToInternalStorage(context: Context, bitmap: android.graphics.Bitmap): Uri {
    val filename = "fish_${System.currentTimeMillis()}.jpg"
    val file = File(context.filesDir, filename)
    
    FileOutputStream(file).use { out ->
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
    }
    
    return Uri.fromFile(file)
} 