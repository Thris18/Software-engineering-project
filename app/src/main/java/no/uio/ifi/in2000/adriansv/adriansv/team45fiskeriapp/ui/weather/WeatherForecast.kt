package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather.LocationWeather
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather.TimeSeriesEntry
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

@Composable
fun WeatherForecast(weather: LocationWeather?) {
    val groupedByDay = weather?.timeseries
        ?.groupBy { it.time.substring(0, 10) } ?: emptyMap()

    val expandedDays = remember { mutableStateMapOf<String, Boolean>() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7)) // lys grå bakgrunn
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        groupedByDay.forEach { (date, entries) ->
            val isExpanded = expandedDays[date] == true

            val filtered = if (isExpanded) entries else filterByMainPeriods(entries)

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
                    ) {


                        Text(
                            text = formatDate(date),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        filtered.forEach { WeatherRow(it) }

                        TextButton(
                            onClick = { expandedDays[date] = !isExpanded },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(if (isExpanded) "Skjul detaljer" else "Detaljer")
                        }
                    }
                }
            }
        }
    }
}


fun formatDate(date: String): String {
    val localDate = LocalDate.parse(date)
    val formatter = DateTimeFormatter.ofPattern("EEEE d. MMMM", Locale("no"))
    return localDate.format(formatter).replaceFirstChar { it.uppercase() }
}

fun filterByMainPeriods(entries: List<TimeSeriesEntry>): List<TimeSeriesEntry> {
    return entries.filter {
        val hour = it.time.substring(11, 13).toInt()
        hour == 3 || hour == 6 || hour == 9 || hour == 12 || hour == 15 || hour == 18
    }
}

fun formatWind(entry: TimeSeriesEntry): String {
    val speed = entry.data.instant.details.windSpeed.toInt()
    val gust = speed + 1
    val direction = entry.data.instant.details.windFromDirection.toInt()
    val arrow = when (direction) {
        in 0..44 -> "↓"
        in 45..89 -> "↙"
        in 90..134 -> "←"
        in 135..179 -> "↖"
        in 180..224 -> "↑"
        in 225..269 -> "↗"
        in 270..314 -> "→"
        else -> "↘"
    }
    return "$speed ($gust) $arrow"
}
