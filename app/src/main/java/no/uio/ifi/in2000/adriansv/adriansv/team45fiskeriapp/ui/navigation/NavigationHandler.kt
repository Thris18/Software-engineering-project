package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.weather.WeatherDataSource
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.weather.WeatherRepository
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components.NavigationBar
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components.SettingsPopup
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components.WelcomeScreen
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components.YourInformationScreen
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.map.MapScreen
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.map.ProfileScreen
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.theme.Team45FiskeriAppTheme
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.weather.WeatherViewModel
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.weather.WeatherViewModelFactory

@Composable
fun NavigationHandler() {
    // Delte tilstander
    var currentRoute by remember { mutableStateOf("welcome") } // Starter med velkomstskjerm
    var isDarkMode by remember { mutableStateOf(true) }
    var showGrib by remember { mutableStateOf(true) }
    var showAlerts by remember { mutableStateOf(true) }
    var showShips by remember { mutableStateOf(true) }

    // Profil-tilstander
    var showSettings by remember { mutableStateOf(false) }
    var showYourInfo by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // Værdata
    val weatherViewModel: WeatherViewModel = viewModel(
        factory = WeatherViewModelFactory(WeatherRepository(WeatherDataSource()))
    )
    val weatherUiState by weatherViewModel.uiState.collectAsStateWithLifecycle()

    // Tema
    Team45FiskeriAppTheme(darkTheme = isDarkMode) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Innholdsområde
                Box(modifier = Modifier.weight(1f)) {
                    when (currentRoute) {
                        "welcome" -> WelcomeScreen(
                            onNavigateToHome = { currentRoute = "kart" }
                        )
                        "kart" -> MapScreen(
                            onNavigateToProfile = { currentRoute = "profil" },
                            currentRoute = currentRoute,
                            onNavigate = { route -> currentRoute = route },
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
                            }
                        )
                        "profil" -> ProfileScreen(
                            userName = "$firstName $lastName",
                            onSettingsClick = { showSettings = true },
                            onYourInformationClick = { showYourInfo = true },
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
        }
    }
}