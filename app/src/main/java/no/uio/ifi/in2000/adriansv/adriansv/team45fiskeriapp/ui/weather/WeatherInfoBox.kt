package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.weather

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather.LocationWeather
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather.WeatherIcon
import java.util.*

private const val TAG = "WeatherInfoBox"

fun isDay(): Int {
    val calendar = Calendar.getInstance()
    return calendar.get(Calendar.HOUR_OF_DAY)
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherInfoBox(
    weather: LocationWeather,
    weatherState: WeatherUiState,
    modifier: Modifier = Modifier
) {
    var showWeatherDetails by remember { mutableStateOf(false) }
    var selectedTimeIndex by remember { mutableStateOf(0) }
    var expanded by remember { mutableStateOf(false) }
    val currentHour = isDay()
    val weatherIcon = WeatherIcon.fromWeatherCode(weather.symbolCode.replace("_day", "").replace("_night", ""), currentHour)
    val context = LocalContext.current

    // Hent tidsperioder fra værvarsel-data og grupper dem etter dato
    val timeOptions = weather.timeseries.map { it.time }
    val selectedTime = timeOptions.getOrNull(selectedTimeIndex)
    val selectedWeatherData = weather.timeseries.getOrNull(selectedTimeIndex)?.data

    // Grupper tidsperioder etter dato
    val groupedTimes = timeOptions.groupBy { time ->
        try {
            val formatter = java.time.format.DateTimeFormatter.ISO_DATE_TIME
            val dateTime = java.time.ZonedDateTime.parse(time, formatter)
            dateTime.toLocalDate()
        } catch (e: Exception) {
            time
        }
    }

    // Finn valgt dato
    val selectedDate = selectedTime?.let { time ->
        try {
            val formatter = java.time.format.DateTimeFormatter.ISO_DATE_TIME
            val dateTime = java.time.ZonedDateTime.parse(time, formatter)
            dateTime.toLocalDate()
        } catch (e: Exception) {
            null
        }
    }

    // Finn valgt tidspunkt for værikon
    val selectedHour = selectedTime?.let { time ->
        try {
            val formatter = java.time.format.DateTimeFormatter.ISO_DATE_TIME
            val dateTime = java.time.ZonedDateTime.parse(time, formatter)
            dateTime.hour
        } catch (e: Exception) {
            currentHour
        }
    } ?: currentHour

    // Oppdater værikon basert på valgt tidspunkt
    val selectedWeatherIcon = WeatherIcon.fromWeatherCode(
        selectedWeatherData?.next_1_hours?.summary?.symbol_code?.replace("_day", "")?.replace("_night", "") ?: weather.symbolCode.replace("_day", "").replace("_night", ""),
        selectedHour
    )

    // Finn nåværende tidspunkt og rund opp til neste time hvis minuttet er over 0
    val currentTime = java.time.ZonedDateTime.now()
    val roundedCurrentTime = currentTime.withMinute(0).withSecond(0).withNano(0)

    // Finn første tilgjengelige tidspunkt etter nåværende tid
    val firstAvailableTimeIndex = timeOptions.indexOfFirst { time ->
        val timeDateTime = java.time.ZonedDateTime.parse(time, java.time.format.DateTimeFormatter.ISO_DATE_TIME)
        val currentDateTime = currentTime.withMinute(0).withSecond(0).withNano(0)
        timeDateTime.hour >= currentDateTime.hour && timeDateTime.toLocalDate().isEqual(currentDateTime.toLocalDate()) ||
        timeDateTime.toLocalDate().isAfter(currentDateTime.toLocalDate())
    }.takeIf { it != -1 } ?: 0

    // Oppdater selectedTimeIndex hvis den er mindre enn firstAvailableTimeIndex
    if (selectedTimeIndex < firstAvailableTimeIndex) {
        selectedTimeIndex = firstAvailableTimeIndex
    }

    FloatingActionButton(
        onClick = { 
            showWeatherDetails = true
            selectedTimeIndex = firstAvailableTimeIndex // Sett til første tilgjengelige tidspunkt når popupen åpnes
        },
        modifier = modifier.width(100.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Temperatur
            Text(
                text = "${weather.temperature.toInt()}°C",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Værikon
            WeatherIconView(weatherIcon = weatherIcon, size = 24.dp)
        }
    }

    if (showWeatherDetails) {
        Dialog(onDismissRequest = { 
            showWeatherDetails = false
            selectedTimeIndex = firstAvailableTimeIndex // Sett til første tilgjengelige tidspunkt når popupen lukkes
        }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header med værikon og tittel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Værikon i detaljvisningen
                        WeatherIconView(weatherIcon = selectedWeatherIcon, size = 48.dp)
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = "Værvarsel",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Dato og tidsperiode velger
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Vis valgt dato
                        Text(
                            text = selectedDate?.let { formatDate(it) } ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            modifier = Modifier.width(120.dp)
                        ) {
                            TextField(
                                value = selectedTime?.let { formatTime(it) } ?: "",
                                onValueChange = { },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .clickable { expanded = !expanded }
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier
                                    .width(120.dp)
                                    .heightIn(max = 300.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            ) {
                                groupedTimes.forEach { (date, times) ->
                                    // Dato header
                                    Text(
                                        text = formatDate(date),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                            .fillMaxWidth()
                                    )
                                    
                                    // Tidsperioder for denne datoen
                                    times.forEachIndexed { index, time ->
                                        val timeIndex = timeOptions.indexOf(time)
                                        val timeDateTime = java.time.ZonedDateTime.parse(time, java.time.format.DateTimeFormatter.ISO_DATE_TIME)
                                        val currentDateTime = currentTime.withMinute(0).withSecond(0).withNano(0)
                                        
                                        // Vis bare tider som er fra nåværende time og fremover
                                        if ((timeDateTime.toLocalDate().isEqual(currentDateTime.toLocalDate()) && 
                                             timeDateTime.hour >= currentDateTime.hour) ||
                                            timeDateTime.toLocalDate().isAfter(currentDateTime.toLocalDate())) {
                                            DropdownMenuItem(
                                                text = { 
                                                    Text(
                                                        text = formatTime(time),
                                                        style = MaterialTheme.typography.bodyLarge
                                                    )
                                                },
                                                onClick = {
                                                    selectedTimeIndex = timeIndex
                                                    expanded = false
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 8.dp)
                                            )
                                        }
                                    }
                                    
                                    if (date != groupedTimes.keys.last()) {
                                        Divider(
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                            thickness = 1.dp,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Værdetaljer
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(16.dp)
                    ) {
                        WeatherDetailRow("Temperatur", "${selectedWeatherData?.instant?.details?.air_temperature?.toInt() ?: 0}°C")
                        WeatherDetailRow("Vindhastighet", "${selectedWeatherData?.instant?.details?.wind_speed ?: 0} m/s")
                        WeatherDetailRow("Vindretning", "${selectedWeatherData?.instant?.details?.wind_from_direction ?: 0}°")
                        WeatherDetailRow("Skydekke", "${selectedWeatherData?.instant?.details?.cloud_area_fraction ?: 0}%")
                        WeatherDetailRow("Nedbør", "${selectedWeatherData?.next_1_hours?.details?.precipitation_amount ?: 0} mm")
                        WeatherDetailRow("Lufttrykk", "${selectedWeatherData?.instant?.details?.air_pressure_at_sea_level ?: 0} hPa")
                    }

                    TextButton(
                        onClick = { 
                            showWeatherDetails = false
                            selectedTimeIndex = firstAvailableTimeIndex // Sett til første tilgjengelige tidspunkt når popupen lukkes
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Lukk")
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatTime(timeString: String): String {
    return try {
        val formatter = java.time.format.DateTimeFormatter.ISO_DATE_TIME
        val time = java.time.ZonedDateTime.parse(timeString, formatter)
        val norwegianFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
        time.format(norwegianFormatter)
    } catch (e: Exception) {
        timeString
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatDate(date: Any): String {
    return when (date) {
        is java.time.LocalDate -> {
            val norwegianFormatter = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")
            date.format(norwegianFormatter)
        }
        else -> date.toString()
    }
}

@Composable
private fun WeatherIconView(weatherIcon: WeatherIcon, size: androidx.compose.ui.unit.Dp) {
    val context = LocalContext.current
    val svgString = remember(weatherIcon.getAssetPath()) {
        try {
            val iconPath = weatherIcon.getAssetPath()
            Log.d(TAG, "Loading weather icon from path: $iconPath")
            val rawSvg = context.assets.open(iconPath).bufferedReader().use { it.readText() }
            """
            <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body {
                            margin: 0;
                            padding: 0;
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            background-color: transparent;
                            width: ${size.value}px;
                            height: ${size.value}px;
                        }
                        svg {
                            width: 100%;
                            height: 100%;
                        }
                    </style>
                </head>
                <body>
                    $rawSvg
                </body>
            </html>
            """
        } catch (e: Exception) {
            Log.e(TAG, "Error loading SVG: ${e.message}")
            ""
        }
    }

    if (svgString.isNotEmpty()) {
        AndroidView(
            factory = { context ->
                android.webkit.WebView(context).apply {
                    settings.javaScriptEnabled = true
                    setBackgroundColor(Color.Transparent.value.toInt())
                    loadDataWithBaseURL(null, svgString, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL(null, svgString, "text/html", "UTF-8", null)
            },
            modifier = Modifier.size(size)
        )
    }
}

@Composable
private fun WeatherDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
        )
    }
} 