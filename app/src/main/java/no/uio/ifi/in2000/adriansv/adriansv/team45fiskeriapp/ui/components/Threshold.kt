package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ThresholdValues(
    val currentSpeed: Float = 0f,
    val windSpeed: Float = 0f,
    val precipitation: Float = 0f,
    val waveHeight: Float = 0f
)

@Composable
fun Threshold(
    modifier: Modifier = Modifier,
    initialValues: ThresholdValues = ThresholdValues(),
    onValuesChange: (ThresholdValues) -> Unit = {}
) {
    var currentSpeed by remember { mutableStateOf(initialValues.currentSpeed) }
    var windSpeed by remember { mutableStateOf(initialValues.windSpeed) }
    var precipitation by remember { mutableStateOf(initialValues.precipitation) }
    var waveHeight by remember { mutableStateOf(initialValues.waveHeight) }

    // Update parent when any value changes
    LaunchedEffect(currentSpeed, windSpeed, precipitation, waveHeight) {
        onValuesChange(
            ThresholdValues(
                currentSpeed = currentSpeed,
                windSpeed = windSpeed,
                precipitation = precipitation,
                waveHeight = waveHeight
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Værparametre",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Current Speed Slider
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Strømhastighet")
                Text(
                    text = "%.1f m/s".format(currentSpeed),
                    fontSize = 14.sp
                )
            }
            Slider(
                value = currentSpeed,
                onValueChange = { currentSpeed = it },
                valueRange = 0f..5f,
                steps = 49,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        // Wind Speed Slider
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Vindhastighet")
                Text(
                    text = "%.1f m/s".format(windSpeed),
                    fontSize = 14.sp
                )
            }
            Slider(
                value = windSpeed,
                onValueChange = { windSpeed = it },
                valueRange = 0f..30f,
                steps = 59,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        // Precipitation Slider
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Nedbør")
                Text(
                    text = "%.1f mm".format(precipitation),
                    fontSize = 14.sp
                )
            }
            Slider(
                value = precipitation,
                onValueChange = { precipitation = it },
                valueRange = 0f..50f,
                steps = 99,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        // Wave Height Slider
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Bølgehøyde")
                Text(
                    text = "%.1f m".format(waveHeight),
                    fontSize = 14.sp
                )
            }
            Slider(
                value = waveHeight,
                onValueChange = { waveHeight = it },
                valueRange = 0f..10f,
                steps = 99,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
} 