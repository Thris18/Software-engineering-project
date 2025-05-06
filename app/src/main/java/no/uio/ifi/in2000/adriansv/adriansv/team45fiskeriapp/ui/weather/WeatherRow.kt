package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.weather

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather.TimeSeriesEntry
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather.WeatherIcon
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter

// Denne funksjonen viser værdata for en spesifikk time, inkludert tid, temperatur, værikon og vindinformasjon, i en horisontal rad
@Composable
fun WeatherRow(entry: TimeSeriesEntry) {
    // Hent og juster tid til lokal tidssone (Europe/Oslo)
    val time = ZonedDateTime.parse(entry.time)
        .withZoneSameInstant(ZoneId.of("Europe/Oslo"))
        .format(DateTimeFormatter.ofPattern("HH:mm"))

    val temp = entry.data.instant.details.air_temperature.toInt()
    val symbolCode = entry.data.next_1_hours?.summary?.symbol_code?.replace("_day", "")?.replace("_night", "") ?: "cloudy"
    val hour = ZonedDateTime.parse(entry.time).hour
    val icon = WeatherIcon.fromWeatherCode(symbolCode, hour)

    // Lag rad for visning av informasjon
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = time,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            color = Color.Black
        )

        // Vise ikon og temperatur
        Row(
            modifier = Modifier.weight(2f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WeatherIconView(weatherIcon = icon, size = 100.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$temp°",
                fontSize = 14.sp,
                color = Color.Black
            )
        }

        // Vise vindinformasjon
        Text(
            text = formatWind(entry),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}



