package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.weather.WeatherDataSource
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.weather.WeatherRepository
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
import android.os.Build
import androidx.annotation.RequiresApi
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.map.AppViewModel
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.weather.WeatherScreen
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.tutorial.rememberTutorialManager
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib.GribViewModel

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun NavigationHandler() {
    val appViewModel: AppViewModel = viewModel()
    
    // Collect state from ViewModel
    val currentRoute by appViewModel.currentRoute.collectAsStateWithLifecycle()
    val isDarkMode by appViewModel.isDarkMode.collectAsStateWithLifecycle()
    val showGrib by appViewModel.showGrib.collectAsStateWithLifecycle()
    val showAlerts by appViewModel.showAlerts.collectAsStateWithLifecycle()
    val showShips by appViewModel.showShips.collectAsStateWithLifecycle()
    val isComingFromWelcome by appViewModel.isComingFromWelcome.collectAsStateWithLifecycle()
    val hasTutorialBeenShown by appViewModel.hasTutorialBeenShown.collectAsStateWithLifecycle()
    val showSettings by appViewModel.showSettings.collectAsStateWithLifecycle()
    val showYourInfo by appViewModel.showYourInfo.collectAsStateWithLifecycle()
    val firstName by appViewModel.firstName.collectAsStateWithLifecycle()
    val lastName by appViewModel.lastName.collectAsStateWithLifecycle()
    val phoneNumber by appViewModel.phoneNumber.collectAsStateWithLifecycle()
    val email by appViewModel.email.collectAsStateWithLifecycle()
    val profileImageUri by appViewModel.profileImageUri.collectAsStateWithLifecycle()

    val tutorialManager = rememberTutorialManager()

    val weatherViewModel: WeatherViewModel = viewModel(
        factory = WeatherViewModelFactory(WeatherRepository.WeatherRepositoryImpl(WeatherDataSource()))
    )
    val weatherUiState by weatherViewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val fishLogViewModel = viewModel { FishLogViewModel(context) }
    val fishLogUiState by fishLogViewModel.uiState.collectAsStateWithLifecycle()

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

    Team45FiskeriAppTheme(darkTheme = isDarkMode) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Innholdsområde
                Box(modifier = Modifier.weight(1f)) {
                    when (currentRoute) {
                        "welcome" -> WelcomeScreen(
                            onNavigateToHome = { 
                                appViewModel.updateCurrentRoute("kart")
                                appViewModel.updateIsComingFromWelcome(!hasTutorialBeenShown)
                            }
                        )
                        "kart" -> MapScreen(
                            onNavigate = { route ->
                                appViewModel.updateCurrentRoute(route)
                                appViewModel.updateIsComingFromWelcome(false)
                            },
                            isDarkMode = isDarkMode,
                            showGrib = showGrib,
                            showAlerts = showAlerts,
                            showShips = showShips,
                            onGribFilterChanged = { newValue ->
                                appViewModel.updateShowGrib(newValue)
                                appViewModel.updateCurrentRoute(currentRoute)
                            },
                            onAlertsFilterChanged = { newValue ->
                                appViewModel.updateShowAlerts(newValue)
                                appViewModel.updateCurrentRoute(currentRoute)
                            },
                            onShipsFilterChanged = { newValue ->
                                appViewModel.updateShowShips(newValue)
                                appViewModel.updateCurrentRoute(currentRoute)
                            },
                            onLocationSelected = if (previousRoute == "fiskelog") { lat, lon, location ->
                                selectedLatitude = lat
                                selectedLongitude = lon
                                selectedLocation = location
                                previousRoute?.let { appViewModel.updateCurrentRoute(it) }
                                showAddFishDialog = true
                                previousRoute = null
                            } else null,
                            isComingFromWelcome = isComingFromWelcome && !hasTutorialBeenShown,
                            onTutorialComplete = {
                                appViewModel.updateHasTutorialBeenShown(true)
                                appViewModel.updateIsComingFromWelcome(false)
                            }
                        )
                        "profil" -> ProfileScreen(
                            firstName = firstName,
                            lastName = lastName,
                            phoneNumber = phoneNumber,
                            email = email,
                            profileImageUri = profileImageUri,
                            onFirstNameChange = { appViewModel.updateFirstName(it) },
                            onLastNameChange = { appViewModel.updateLastName(it) },
                            onPhoneNumberChange = { appViewModel.updatePhoneNumber(it) },
                            onEmailChange = { appViewModel.updateEmail(it) },
                            onProfileImageChange = { appViewModel.updateProfileImageUri(it) },
                            isDarkMode = isDarkMode,
                            showGrib = showGrib,
                            showAlerts = showAlerts,
                            showShips = showShips,
                            onDarkModeChange = { newValue ->
                                appViewModel.updateDarkMode(newValue)
                                appViewModel.updateCurrentRoute(currentRoute)
                            },
                            onGribFilterChanged = { newValue ->
                                appViewModel.updateShowGrib(newValue)
                                appViewModel.updateCurrentRoute(currentRoute)
                            },
                            onAlertsFilterChanged = { newValue ->
                                appViewModel.updateShowAlerts(newValue)
                                appViewModel.updateCurrentRoute(currentRoute)
                            },
                            onShipsFilterChanged = { newValue ->
                                appViewModel.updateShowShips(newValue)
                                appViewModel.updateCurrentRoute(currentRoute)
                            },
                            onFishLogClick = { appViewModel.updateCurrentRoute("fiskelog") }
                        )
                        "fiskelog" -> FishLogScreen(
                            fishLogs = fishLogUiState.fishLogs,
                            onBackClick = { appViewModel.updateCurrentRoute("profil") },
                            onAddFish = { 
                                showAddFishDialog = true
                                previousRoute = currentRoute
                            },
                            onRemoveFish = { fishLog ->
                                fishLogViewModel.removeFishLog(fishLog)
                            }
                        )
                        "vaer" -> WeatherScreen(weather = weatherUiState.weather)
                    }
                }

                if (currentRoute != "welcome") {
                    NavigationBar(
                        currentRoute = currentRoute,
                        onNavigate = { route -> appViewModel.updateCurrentRoute(route) },
                        tutorialManager = tutorialManager
                    )
                }
            }

            if (showSettings) {
                val gribViewModel: GribViewModel = viewModel()
                SettingsPopup(
                    context = context,
                    isDarkMode = isDarkMode,
                    showGrib = showGrib,
                    showAlerts = showAlerts,
                    showShips = showShips,
                    onDarkModeChange = { newDarkMode ->
                        appViewModel.updateDarkMode(newDarkMode)
                    },
                    onGribFilterChanged = { newValue ->
                        appViewModel.updateShowGrib(newValue)
                        appViewModel.updateCurrentRoute(currentRoute)
                    },
                    onAlertsFilterChanged = { newValue ->
                        appViewModel.updateShowAlerts(newValue)
                        appViewModel.updateCurrentRoute(currentRoute)
                    },
                    onShipsFilterChanged = { newValue ->
                        appViewModel.updateShowShips(newValue)
                        appViewModel.updateCurrentRoute(currentRoute)
                    },
                    onDismiss = { appViewModel.updateShowSettings(show = false) },
                    gribViewModel = gribViewModel
                )
            }

            if (showYourInfo) {
                YourInformationScreen(
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumber = phoneNumber,
                    email = email,
                    onFirstNameChange = { appViewModel.updateFirstName(it) },
                    onLastNameChange = { appViewModel.updateLastName(it) },
                    onPhoneNumberChange = { appViewModel.updatePhoneNumber(it) },
                    onEmailChange = { appViewModel.updateEmail(it) },
                    onBackClick = { appViewModel.updateShowYourInfo(false) }
                )
            }

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
                        appViewModel.updateCurrentRoute("kart")
                        showAddFishDialog = false
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
                    selectedLocationForFish = selectedLocation
                )
            }
        }
    }
}