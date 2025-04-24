package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components.NavigationBar
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components.SettingsPopup
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components.YourInformationScreen
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.map.MapScreen
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.map.ProfileScreen
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.theme.Team45FiskeriAppTheme

@Composable
fun NavigationHandler() {
    // Shared states
    var currentRoute by remember { mutableStateOf("kart") }
    var isDarkMode by remember { mutableStateOf(true) }
    var showGrib by remember { mutableStateOf(true) }
    var showAlerts by remember { mutableStateOf(true) }
    var showShips by remember { mutableStateOf(true) }
    
    // Profile states
    var showSettings by remember { mutableStateOf(false) }
    var showYourInfo by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // Theme
    Team45FiskeriAppTheme(darkTheme = isDarkMode) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Content area
                Box(modifier = Modifier.weight(1f)) {
                    when (currentRoute) {
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
                
                // Persistent navigation bar at the bottom
                NavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route -> currentRoute = route }
                )
            }

            // Popups
            if (showSettings) {
                SettingsPopup(
                    isDarkMode = isDarkMode,
                    showGrib = showGrib,
                    showAlerts = showAlerts,
                    showShips = showShips,
                    onDarkModeChange = { newValue ->
                        isDarkMode = newValue
                        // Oppdater tema umiddelbart
                        currentRoute = currentRoute // Dette vil trigge en recomposition med nytt tema
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