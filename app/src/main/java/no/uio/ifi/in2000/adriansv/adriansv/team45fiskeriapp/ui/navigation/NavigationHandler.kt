package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.fish.FishLogRepository
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.weather.WeatherDataSource
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.weather.WeatherRepository
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.fish.FishLog
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components.NavigationBar
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components.SettingsPopup
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components.WelcomeScreen
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components.YourInformationScreen
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fish.AddFishDialog
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fish.FishLogScreen
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fish.FishLogViewModel
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.map.MapScreen
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.map.ProfileScreen
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.theme.Team45FiskeriAppTheme
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.weather.WeatherViewModel
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.weather.WeatherViewModelFactory
import android.net.Uri

@Composable
fun NavigationHandler() {
    // Delte tilstander
    var currentRoute by remember { mutableStateOf("welcome") } // Starter med velkomstskjerm
    var isDarkMode by remember { mutableStateOf(false) }
    var showGrib by remember { mutableStateOf(true) }
    var showAlerts by remember { mutableStateOf(true) }
    var showShips by remember { mutableStateOf(true) }
    
    // Holder styr på om navigasjon kommer fra welcome screen
    var isComingFromWelcome by remember { mutableStateOf(false) }
    
    // Holder styr på om tutorialen er vist (persisteres gjennom recomposition)
    var hasTutorialBeenShown by remember { mutableStateOf(false) }

    // Profil-tilstander
    var showSettings by remember { mutableStateOf(false) }
    var showYourInfo by remember { mutableStateOf(false) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var profileImageUri by remember { mutableStateOf<String?>(null) }

    // Værdata
    val weatherViewModel: WeatherViewModel = viewModel(
        factory = WeatherViewModelFactory(WeatherRepository(WeatherDataSource()))
    )
    val weatherUiState by weatherViewModel.uiState.collectAsStateWithLifecycle()

    // Fiskelog
    val context = LocalContext.current
    val fishLogViewModel = viewModel { FishLogViewModel(context) }
    val fishLogUiState by fishLogViewModel.uiState.collectAsStateWithLifecycle()

    // AddFishDialog tilstander
    var showAddFishDialog by remember { mutableStateOf(false) }
    var selectedLatitude by remember { mutableStateOf(0.0) }
    var selectedLongitude by remember { mutableStateOf(0.0) }
    var selectedLocation by remember { mutableStateOf<String?>(null) }
    var previousRoute by remember { mutableStateOf<String?>(null) }
    var fishType by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Tema
    Team45FiskeriAppTheme(darkTheme = isDarkMode) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Innholdsområde
                Box(modifier = Modifier.weight(1f)) {
                    when (currentRoute) {
                        "welcome" -> WelcomeScreen(
                            onNavigateToHome = { 
                                currentRoute = "kart"
                                // Kun sett flagget hvis tutorialen ikke er vist tidligere
                                isComingFromWelcome = !hasTutorialBeenShown
                            }
                        )
                        "kart" -> MapScreen(
                            onNavigateToProfile = { 
                                currentRoute = "profil"
                                isComingFromWelcome = false  // Reset flagg når vi navigerer videre
                            },
                            currentRoute = currentRoute,
                            onNavigate = { route -> 
                                currentRoute = route
                                isComingFromWelcome = false  // Reset flagg ved navigasjon
                            },
                            isDarkMode = isDarkMode,
                            showGrib = showGrib,
                            showAlerts = showAlerts,
                            showShips = showShips,
                            onGribFilterChanged = { newValue ->
                                showGrib = newValue
                                currentRoute = currentRoute
                            },
                            onAlertsFilterChanged = { newValue ->
                                showAlerts = newValue
                                currentRoute = currentRoute
                            },
                            onShipsFilterChanged = { newValue ->
                                showShips = newValue
                                currentRoute = currentRoute
                            },
                            onLocationSelected = if (previousRoute == "fiskelog") { lat, lon, location ->
                                selectedLatitude = lat
                                selectedLongitude = lon
                                selectedLocation = location
                                previousRoute?.let { currentRoute = it }
                                showAddFishDialog = true
                                previousRoute = null
                            } else null,
                            isComingFromWelcome = isComingFromWelcome && !hasTutorialBeenShown,
                            // Når tutorialen er ferdig
                            onTutorialComplete = {
                                hasTutorialBeenShown = true
                                isComingFromWelcome = false
                            }
                        )
                        "profil" -> ProfileScreen(
                            firstName = firstName,
                            lastName = lastName,
                            phoneNumber = phoneNumber,
                            email = email,
                            profileImageUri = profileImageUri,
                            onFirstNameChange = { firstName = it },
                            onLastNameChange = { lastName = it },
                            onPhoneNumberChange = { phoneNumber = it },
                            onEmailChange = { email = it },
                            onProfileImageChange = { profileImageUri = it },
                            onSettingsClick = { showSettings = true },
                            isDarkMode = isDarkMode,
                            showGrib = showGrib,
                            showAlerts = showAlerts,
                            showShips = showShips,
                            onDarkModeChange = { newValue ->
                                isDarkMode = newValue
                                currentRoute = currentRoute
                            },
                            onGribFilterChanged = { newValue ->
                                showGrib = newValue
                                currentRoute = currentRoute
                            },
                            onAlertsFilterChanged = { newValue ->
                                showAlerts = newValue
                                currentRoute = currentRoute
                            },
                            onShipsFilterChanged = { newValue ->
                                showShips = newValue
                                currentRoute = currentRoute
                            },
                            onFishLogClick = { currentRoute = "fiskelog" }
                        )
                        "fiskelog" -> FishLogScreen(
                            fishLogs = fishLogUiState.fishLogs,
                            onBackClick = { currentRoute = "profil" },
                            onAddFish = { 
                                showAddFishDialog = true
                                previousRoute = currentRoute
                            },
                            onRemoveFish = { fishLog ->
                                fishLogViewModel.removeFishLog(fishLog)
                            }
                        )
                    }
                }

                // Navigasjonsmeny nederst – skjules på velkomstskjermen
                if (currentRoute != "welcome") {
                    NavigationBar(
                        currentRoute = currentRoute,
                        onNavigate = { route -> currentRoute = route },
                        weather = weatherUiState.weather,
                        weatherState = weatherUiState
                    )
                }
            }

            // Innstillinger-popup
            if (showSettings) {
                SettingsPopup(
                    isDarkMode = isDarkMode,
                    showGrib = showGrib,
                    showAlerts = showAlerts,
                    showShips = showShips,
                    onDarkModeChange = { newValue ->
                        isDarkMode = newValue
                        currentRoute = currentRoute
                    },
                    onGribFilterChanged = { newValue ->
                        showGrib = newValue
                        currentRoute = currentRoute
                    },
                    onAlertsFilterChanged = { newValue ->
                        showAlerts = newValue
                        currentRoute = currentRoute
                    },
                    onShipsFilterChanged = { newValue ->
                        showShips = newValue
                        currentRoute = currentRoute
                    },
                    onDismiss = { showSettings = false }
                )
            }

            // Brukerinformasjon-popup
            if (showYourInfo) {
                YourInformationScreen(
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumber = phoneNumber,
                    email = email,
                    onFirstNameChange = { firstName = it },
                    onLastNameChange = { lastName = it },
                    onPhoneNumberChange = { phoneNumber = it },
                    onEmailChange = { email = it },
                    onBackClick = { showYourInfo = false }
                )
            }

            // AddFishDialog
            if (showAddFishDialog) {
                AddFishDialog(
                    onDismiss = { 
                        showAddFishDialog = false
                        previousRoute = null
                    },
                    onAddFish = { fishLog ->
                        fishLogViewModel.addFishLog(fishLog)
                        showAddFishDialog = false
                        // Nullstill all informasjon
                        fishType = ""
                        location = ""
                        area = ""
                        description = ""
                        weight = ""
                        imageUri = null
                        selectedLatitude = 0.0
                        selectedLongitude = 0.0
                        selectedLocation = null
                        previousRoute = null
                    },
                    latitude = selectedLatitude,
                    longitude = selectedLongitude,
                    onSelectLocation = {
                        previousRoute = currentRoute
                        currentRoute = "kart"
                        showAddFishDialog = false
                    },
                    selectedLocationForFish = selectedLocation,
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
                    onImageUriChange = { imageUri = it }
                )
            }
        }
    }
}