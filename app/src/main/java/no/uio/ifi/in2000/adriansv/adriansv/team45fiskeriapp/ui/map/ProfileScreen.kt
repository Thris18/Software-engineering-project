package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.map

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components.ProfilePopup
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components.SettingsPopup
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components.YourInformationScreen

@Composable
fun ProfileScreen(
    userName: String = "",
    onSettingsClick: () -> Unit,
    onYourInformationClick: () -> Unit,
    isDarkMode: Boolean,
    showGrib: Boolean,
    showAlerts: Boolean,
    showShips: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onGribFilterChanged: (Boolean) -> Unit,
    onAlertsFilterChanged: (Boolean) -> Unit,
    onShipsFilterChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSettings by remember { mutableStateOf(false) }
    var showYourInfo by remember { mutableStateOf(false) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    Box(modifier = modifier.fillMaxSize()) {
        // Main profile content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Profil",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Your Information Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { showYourInfo = true },
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 1.dp
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
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text(
                                text = if (firstName.isNotEmpty() || lastName.isNotEmpty()) 
                                    "$firstName $lastName".trim() 
                                else 
                                    "Din informasjon",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Gå til Din informasjon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 1.dp
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
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Innstillinger",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Gå til Innstillinger",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                onFirstNameChange = { firstName = it },
                onLastNameChange = { lastName = it },
                onPhoneNumberChange = { phoneNumber = it },
                onEmailChange = { email = it },
                onBackClick = { showYourInfo = false }
            )
        }

        // Settings Screen
        if (showSettings) {
            SettingsPopup(
                isDarkMode = isDarkMode,
                showGrib = showGrib,
                showAlerts = showAlerts,
                showShips = showShips,
                onDarkModeChange = onDarkModeChange,
                onGribFilterChanged = onGribFilterChanged,
                onAlertsFilterChanged = onAlertsFilterChanged,
                onShipsFilterChanged = onShipsFilterChanged,
                onDismiss = { showSettings = false }
            )
        }
    }
} 