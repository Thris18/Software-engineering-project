package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.map

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.R
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components.SettingsPopup
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components.YourInformationScreen
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fishing.FishingTripsScreen
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib.GribViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * PROFILSKJERMEN FOR FISKERIAPPLIKASJONEN
 * 
 * Denne komponenten håndterer brukerens profilside og fungerer som et knutepunkt for 
 * brukerens personlige data og innstillinger.
 * 
 * Nøkkelfunksjonalitet:
 * - Visning og redigering av brukerens personlige informasjon (navn, telefon, e-post)
 * - Administrering av profilbilde
 * - Tilgang til fiskeloggen med oversikt over fangster
 * - Tilgang til oversikt over brukerens fisketurer
 * - Tilgang til appinnstillinger (mørk/lys modus, visning av GRIB-data, varsler, skip)
 * 
 * Filen håndterer:
 * - Lagring av brukerdata i SharedPreferences
 * - Navigasjon til andre undersider (fiskelogg, fisketurer, innstillinger)
 * - Opplasting og visning av profilbilder
 * - Brukergrensesnitt med kort for hver hovedfunksjon
 * 
 * Skjermen fungerer som et personlig dashbord for fiskeren, med enkel tilgang 
 * til alle brukerrelaterte funksjoner i applikasjonen.
 */

@Composable
fun ProfileScreen(
    firstName: String,
    lastName: String,
    phoneNumber: String,
    email: String,
    profileImageUri: String?,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onProfileImageChange: (String) -> Unit,
    isDarkMode: Boolean,
    showGrib: Boolean,
    showAlerts: Boolean,
    showShips: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onGribFilterChanged: (Boolean) -> Unit,
    onAlertsFilterChanged: (Boolean) -> Unit,
    onShipsFilterChanged: (Boolean) -> Unit,
    onFishLogClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSettings by remember { mutableStateOf(false) }
    var showYourInfo by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }
    var showFishingTrips by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("user_info", Context.MODE_PRIVATE) }
    val gribViewModel: GribViewModel = viewModel()

    // Load saved user information from SharedPreferences
    LaunchedEffect(Unit) {
        val savedFirstName = sharedPreferences.getString("firstName", "") ?: ""
        val savedLastName = sharedPreferences.getString("lastName", "") ?: ""
        val savedPhoneNumber = sharedPreferences.getString("phoneNumber", "") ?: ""
        val savedEmail = sharedPreferences.getString("email", "") ?: ""

        onFirstNameChange(savedFirstName)
        onLastNameChange(savedLastName)
        onPhoneNumberChange(savedPhoneNumber)
        onEmailChange(savedEmail)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onProfileImageChange(it.toString())
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Main profile content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Header with profile title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profil",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
            }

            // Profile Image with change option
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { showImagePicker = true }
                    .align(Alignment.CenterHorizontally)
            ) {
                if (profileImageUri != null) {
                    AsyncImage(
                        model = profileImageUri,
                        contentDescription = "Profilbilde",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.sailor_mascot),
                        contentDescription = "Profilbilde",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                
                // Camera icon overlay
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Endre profilbilde",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Your Information Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { showYourInfo = true },
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text(
                                text = if (firstName.isNotEmpty() || lastName.isNotEmpty()) 
                                    "$firstName $lastName".trim() 
                                else 
                                    "Din informasjon",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (firstName.isNotEmpty() || lastName.isNotEmpty()) {
                                Text(
                                    text = "Din informasjon",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Gå til Din informasjon",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fish Log Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { onFishLogClick() },
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text(
                                text = "Fiskelogg",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Se dine fangster",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Gå til Fiskelog",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fishtrips Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { showFishingTrips = true },
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBoat,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text(
                                text = "Mine fisketurer",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Se alle dine fisketurer",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Gå til Mine fisketurer",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Settings Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { showSettings = true },
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Innstillinger",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Gå til Innstillinger",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Your Information Screen
        if (showYourInfo) {
            YourInformationScreen(
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phoneNumber,
                email = email,
                onFirstNameChange = onFirstNameChange,
                onLastNameChange = onLastNameChange,
                onPhoneNumberChange = onPhoneNumberChange,
                onEmailChange = onEmailChange,
                onBackClick = { showYourInfo = false }
            )
        }

        // Settings Screen
        if (showSettings) {
            SettingsPopup(
                context = context,
                isDarkMode = isDarkMode,
                showGrib = showGrib,
                showAlerts = showAlerts,
                showShips = showShips,
                onDarkModeChange = onDarkModeChange,
                onGribFilterChanged = onGribFilterChanged,
                onAlertsFilterChanged = onAlertsFilterChanged,
                onShipsFilterChanged = onShipsFilterChanged,
                onDismiss = { showSettings = false },
                gribViewModel = gribViewModel
            )
        }

        // Launch image picker when showImagePicker is true
        if (showImagePicker) {
            launcher.launch("image/*")
            showImagePicker = false
        }

        // Fishing Trips Screen
        if (showFishingTrips) {
            FishingTripsScreen(
                onBack = { showFishingTrips = false }
            )
            return
        }
    }
} 