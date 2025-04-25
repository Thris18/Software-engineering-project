package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.R
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather.LocationWeather
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.weather.WeatherInfoBox
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.weather.WeatherUiState

@Composable
fun NavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    weather: LocationWeather? = null,
    weatherState: WeatherUiState? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RectangleShape,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Kart-knapp
            NavigationItem(
                icon = painterResource(id = R.drawable.map_marker),
                label = "Kart",
                selected = currentRoute == "kart",
                onClick = { onNavigate("kart") }
            )

            // Vær-knapp
            if (weather != null && weatherState != null) {
                WeatherInfoBox(
                    weather = weather,
                    weatherState = weatherState,
                    modifier = Modifier
                )
            }

            // Profil-knapp
            NavigationItem(
                icon = Icons.Default.Person,
                label = "Profil",
                selected = currentRoute == "profil",
                onClick = { onNavigate("profil") }
            )
        }
    }
}

@Composable
private fun NavigationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .height(56.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun NavigationItem(
    icon: androidx.compose.ui.graphics.painter.Painter,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .height(56.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                painter = icon,
                contentDescription = label,
                tint = if (selected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
} 