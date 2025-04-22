package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.farevarsel

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib.GribData

@Composable
fun AlertInfoCard(
    alertData: GribData,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Værvarsel") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("LatLon_Projection: ${alertData.lat}, ${alertData.lon}")
                Text("Pressure_height_above_ground: ${alertData.pressure} hPa")
                Text("u-component_of_wind_height_above_ground: ${alertData.windSpeed} m/s")
                Text("v-component_of_wind_height_above_ground: ${alertData.windDirection}°")
                Text("Total_precipitation_height_above_ground: ${alertData.precipitation} mm")
                Text("lat: ${alertData.lat}°")
                Text("lon: ${alertData.lon}°")
                Text("time: ${alertData.time}")
                Text("reftime: ${alertData.reftime}")
                Text("height_above_ground: ${alertData.height_above_ground} m")
                Text("height_above_ground1: ${alertData.height_above_ground1} m")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Lukk")
            }
        }
    )
}

