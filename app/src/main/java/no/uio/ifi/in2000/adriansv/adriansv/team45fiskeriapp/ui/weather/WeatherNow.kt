@file:Suppress("DEPRECATION")

package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.weather

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather.LocationWeather

@Composable
fun WeatherNow(weather: LocationWeather?) {
    weather ?: return

    Log.d("WeatherSymbolCode", "Current weather symbol code: ${weather.symbolCode}")

    // Dynamic background color based on weather
    val weatherAnimation = when (weather.symbolCode) {

        // Clear
        "clearsky_day" -> "lottie_weather_files/weather_sun.json"
        "clearsky_night" -> "lottie_weather_files/weather_night.json"

        // Partly cloud
        "fair_day", "partlycloudy_day" -> "lottie_weather_files/weather_fair.json"
        "fair_night", "partlycloudy_night", "cloudy_night" -> "lottie_weather_files/weather_cloudynight.json"

        // Cloudy
        "cloudy_day" -> "lottie_weather_files/weather_cloudy.json"

        // Rain
        "rain_day", "lightrain_day", "heavyrain_day" -> "lottie_weather_files/weather_rain.json"

        // Thunder/lightning
        "lightning_day", "lightning_night" -> "lottie_weather_files/weather_lightning.json"
        "heavyrainandthunder_day", "heavyrainandthunder_night" -> "lottie_weather_files/weather_thunderandrain.json"

        // Snow
        "snow_day", "heavysnow_day", "snow_night", "heavysnow_night" -> "lottie_weather_files/weather_snow.json"

        // Fog
        "fog_day" -> "lottie_weather_files/weather_fog.json"

        else -> "lottie_weather_files/weather_cloudy.json"
    }
    // Load the animation
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.Asset(weatherAnimation))

    // Loop animation and get progress
    val progress by animateLottieCompositionAsState(
        composition,
        isPlaying = true,
        iterations = LottieConstants.IterateForever
    )

    val backgroundColor = when (weather.symbolCode) {

        "clearsky_day" -> Color(0xFF87CEEB)
        "clearsky_night" -> Color(0xFF0D1B2A)

        "fair_day", "partlycloudy_day" -> Color(0xFFAEDFF7)
        "fair_night", "partlycloudy_night" -> Color(0xFF1C2D40)

        "cloudy_day" -> Color(0xFFB0BEC5)
        "cloudy_night" -> Color(0xFF2F3E46)

        "lightrain_day", "rain_day", "heavyrain_day" -> Color(0xFF607D8B)
        "lightrain_night", "rain_night", "heavyrain_night" -> Color(0xFF263238)

        "snow_day", "heavysnow_day" -> Color(0xFFECEFF1)
        "snow_night", "heavysnow_night" -> Color(0xFF90A4AE)

        "fog_day" -> Color(0xFFCFD8DC)
        "fog_night" -> Color(0xFF37474F)

        "lightning_day", "heavyrainandthunder_day" -> Color(0xFF455A64)
        "lightning_night", "heavyrainandthunder_night" -> Color(0xFF263238)

        else -> Color(0xFF90CAF9)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "${weather.temperature.toInt()}°",
            fontSize = 100.sp,
            color = Color.Black,
            modifier = Modifier.padding(top = 24.dp)
        )


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoLine("Føles som", "${weather.temperature.toInt()}°")
            InfoLine(
                "Vind",
                "${weather.windSpeed} m/s " + when (weather.windDirection.toInt()) {
                    in 0..44 -> "↓"
                    in 45..89 -> "↙"
                    in 90..134 -> "←"
                    in 135..179 -> "↖"
                    in 180..224 -> "↑"
                    in 225..269 -> "↗"
                    in 270..314 -> "→"
                    else -> "↘"
                }
            )
            InfoLine("Nedbør neste time", "${weather.precipitationAmount} mm")
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .padding(top = 40.dp)
        ) {
            LottieAnimation(
                composition = composition,
                progress = progress,
                modifier = Modifier
                    .align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Text(
        text = "$label: $value",
        color = Color.Black,
        style = MaterialTheme.typography.bodyLarge
    )
}