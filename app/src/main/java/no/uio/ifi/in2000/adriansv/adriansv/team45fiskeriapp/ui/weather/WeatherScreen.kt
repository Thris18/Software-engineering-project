package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather.LocationWeather

// This function is responsible for displaying the weather screen
@Composable
fun WeatherScreen(
    weather: LocationWeather?,
    viewModel: WeatherViewModel = viewModel(),
    mapCenter: org.maplibre.android.geometry.LatLng? = null
) {
    var selectedTab by remember { mutableStateOf(0) }

    // Oppdater værdata når mapCenter endres
    LaunchedEffect(mapCenter) {
        if (mapCenter != null) {
            viewModel.updateWeather(mapCenter.latitude, mapCenter.longitude, 12.0)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        when (selectedTab) {
            0 -> WeatherNow(weather)
            1 -> WeatherForecast(weather)
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .background(Color(0xFFEAF0F7), shape = RoundedCornerShape(32.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WeatherToggleButton("Nå", selectedTab == 0) { selectedTab = 0 }
            WeatherToggleButton("10 dager", selectedTab == 1) { selectedTab = 1 }
        }
    }
}

