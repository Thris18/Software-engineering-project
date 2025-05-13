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

    // Dynamisk valg av animasjon basert på vær
    val weatherAnimation = when (weather.symbolCode) {

        // Klart vær
        "clearsky_day" -> "lottie_weather_files/weather_sun.json"
        "clearsky_night" -> "lottie_weather_files/weather_night.json"

        // Delvis skyet
        "fair_day", "partlycloudy_day" -> "lottie_weather_files/weather_fair.json"
        "fair_night", "partlycloudy_night", "cloudy_night" -> "lottie_weather_files/weather_cloudynight.json"

        // Skyet
        "cloudy_day" -> "lottie_weather_files/weather_cloudy.json"

        // Regn
        "rain_day", "lightrain_day", "heavyrain_day" -> "lottie_weather_files/weather_rain.json"

        // Torden
        "lightning_day", "lightning_night" -> "lottie_weather_files/weather_lightning.json"
        "heavyrainandthunder_day", "heavyrainandthunder_night" -> "lottie_weather_files/weather_thunderandrain.json"

        // Snø
        "snow_day", "heavysnow_day", "snow_night", "heavysnow_night" -> "lottie_weather_files/weather_snow.json"

        // Tåke
        "fog_day" -> "lottie_weather_files/weather_fog.json"

        else -> "lottie_weather_files/weather_cloudy.json" // Standard til skyet
    }
    // Last animasjonen fra assets
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.Asset(weatherAnimation))

    // Loop animasjonen kontinuerlig
    val progress by animateLottieCompositionAsState(
        composition,
        isPlaying = true, // Sørger for at animasjonen spiller
        iterations = LottieConstants.IterateForever // Sørger for at animasjonen går uendelig
    )

    // Dynamisk bakgrunnsfarge basert på vær
    val backgroundColor = when (weather.symbolCode) {

        "clearsky_day" -> Color(0xFF87CEEB) // Lys blå (klar himmel)
        "clearsky_night" -> Color(0xFF0D1B2A) // Mørk blåsvart

        "fair_day", "partlycloudy_day" -> Color(0xFFAEDFF7)
        "fair_night", "partlycloudy_night" -> Color(0xFF1C2D40)

        "cloudy_day" -> Color(0xFFB0BEC5) // Grå
        "cloudy_night" -> Color(0xFF2F3E46) // Mørk gråblå

        "lightrain_day", "rain_day", "heavyrain_day" -> Color(0xFF607D8B)
        "lightrain_night", "rain_night", "heavyrain_night" -> Color(0xFF263238)

        "snow_day", "heavysnow_day" -> Color(0xFFECEFF1)
        "snow_night", "heavysnow_night" -> Color(0xFF90A4AE)

        "fog_day" -> Color(0xFFCFD8DC)
        "fog_night" -> Color(0xFF37474F)

        "lightning_day", "heavyrainandthunder_day" -> Color(0xFF455A64)
        "lightning_night", "heavyrainandthunder_night" -> Color(0xFF263238)

        else -> Color(0xFF90CAF9) // Standard blå bakgrunn
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor) // Bakgrunnsfarge endret dynamisk
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp) // Plass mellom de forskjellige tekstene og elementene
    ) {
        // Temperatur - Større font og venstrejustert
        Text(
            text = "${weather.temperature.toInt()}°",
            fontSize = 100.sp, // Større tekst
            color = Color.Black,
            modifier = Modifier.padding(top = 24.dp) // Litt avstand til toppen
        )

        // Informasjonslinjer under temperaturen
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp), // Litt avstand fra temperatur
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoLine("Føles som", "${weather.temperature.toInt()}°")
            InfoLine(
                "Vind",
                "${weather.windSpeed} m/s fra ${weather.windDirection}° " + when (weather.windDirection.toInt()) {
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

        // Større Lottie animasjon midtstilt under tekstlinjene
        Box(
            modifier = Modifier
                .fillMaxWidth() // Bredde på animasjonen
                .height(350.dp) // Øk høyden på animasjonen
                .padding(top = 40.dp) // Økt avstand mellom tekst og animasjon
        ) {
            LottieAnimation(
                composition = composition,
                progress = progress,
                modifier = Modifier
                    .align(Alignment.Center) // Midtstille animasjonen
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