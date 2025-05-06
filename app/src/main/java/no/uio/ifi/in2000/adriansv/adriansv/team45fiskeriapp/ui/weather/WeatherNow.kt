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
        "clearsky_day" -> "lottie_weather_files/weather_sun.json" // Solskinn
        "rainy_day", "09d", "10d", "11d" -> "lottie_weather_files/weather_rain.json" // Regn
        "cloudy_day" -> "lottie_weather_files/weather_cloudy.json" // Skyet
        "fair_day" -> "lottie_weather_files/weather_fair.json" // Delvis skyet
        "partlycloudy" -> "lottie_weather_files/weather_fair.json" // Delvis skyet
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
        "clearsky_day" -> Color(0xFF87CEEB) // Blå himmel for klart vær
        "cloudy_day" -> Color(0xFFB0BEC5) // Grå himmel for overskyet vær
        "rainy_day" -> Color(0xFF607D8B) // Mørkere gråblå for regn
        "snowy_day" -> Color(0xFF80DEEA) // Lys blå for snø
        else -> Color(0xFF87CEEB) // Standard blå bakgrunn
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
            InfoLine("Vind", "${weather.windSpeed} m/s fra ${weather.windDirection}°")
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