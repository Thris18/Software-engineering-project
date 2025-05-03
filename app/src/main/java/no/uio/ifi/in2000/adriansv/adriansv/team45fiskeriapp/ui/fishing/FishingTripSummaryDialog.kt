package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fishing

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.fish.FishLog
import org.maplibre.android.geometry.LatLng
import java.time.Duration
import java.time.LocalDateTime
import android.graphics.Bitmap
import android.view.View
import org.maplibre.android.maps.MapView
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

@Composable
fun FishingTripSummaryDialog(
    onDismiss: () -> Unit,
    tripName: String,
    startTime: LocalDateTime,
    endTime: LocalDateTime,
    route: List<LatLng>,
    catches: List<FishLog>,
    mapView: MapView?,
    mapScreenshot: Uri?
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = "Oppsummering av $tripName",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Varighet
                val duration = Duration.between(startTime, endTime)
                val hours = duration.toHours()
                val minutes = duration.toMinutesPart()
                val seconds = duration.toSecondsPart()
                
                Text(
                    text = "Varighet: ${String.format("%02d:%02d:%02d", hours, minutes, seconds)}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Kart screenshot
                mapScreenshot?.let { uri ->
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = "Fisketur rute",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Fangster
                if (catches.isNotEmpty()) {
                    Text(
                        text = "Fangster",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(catches) { fishLog ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        text = fishLog.fishType,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "Vekt: ${fishLog.weight}kg",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (fishLog.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = fishLog.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                    fishLog.imageUri?.let { uri ->
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Image(
                                            painter = rememberAsyncImagePainter(uri),
                                            contentDescription = "Fangst bilde",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(100.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Lukk knapp
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Lukk")
                }
            }
        }
    }
} 