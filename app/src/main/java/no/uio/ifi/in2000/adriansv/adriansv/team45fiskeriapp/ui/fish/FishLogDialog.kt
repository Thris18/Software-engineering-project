package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fish

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.fish.FishLog
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

@Composable
fun FishLogDialog(
    fishLogs: List<FishLog>,
    onDismiss: () -> Unit,
    onClearLogs: () -> Unit,
    onAddFish: () -> Unit,
    selectedLocation: Pair<Double, Double>?,
    failedImageLoads: Set<String>,
    onImageLoadError: (String) -> Unit,
    onRemoveFish: (FishLog) -> Unit
) {
    val selectedFishLog = if (selectedLocation != null) {
        fishLogs.find { it.latitude == selectedLocation.first && it.longitude == selectedLocation.second }
    } else null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (selectedFishLog != null) "Fiskedetaljer" else "Fiskelogg")
                if (selectedFishLog == null) {
                    IconButton(
                        onClick = onAddFish,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Legg til fisk",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedFishLog != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            InfoSection(
                                title = "Fisketype",
                                value = selectedFishLog.fishType,
                                color = MaterialTheme.colorScheme.onSurface,
                                titleStyle = MaterialTheme.typography.bodyMedium,
                                valueStyle = MaterialTheme.typography.bodySmall
                            )

                            Divider(modifier = Modifier.padding(vertical = 4.dp))

                            InfoSection(
                                title = "Sted",
                                value = selectedFishLog.location,
                                titleStyle = MaterialTheme.typography.bodyMedium,
                                valueStyle = MaterialTheme.typography.bodySmall
                            )

                            Divider(modifier = Modifier.padding(vertical = 4.dp))

                            InfoSection(
                                title = "Område",
                                value = selectedFishLog.area,
                                titleStyle = MaterialTheme.typography.bodyMedium,
                                valueStyle = MaterialTheme.typography.bodySmall
                            )

                            if (selectedFishLog.description.isNotBlank()) {
                                Divider(modifier = Modifier.padding(vertical = 4.dp))
                                InfoSection(
                                    title = "Beskrivelse",
                                    value = selectedFishLog.description,
                                    titleStyle = MaterialTheme.typography.bodyMedium,
                                    valueStyle = MaterialTheme.typography.bodySmall
                                )
                            }

                            if (selectedFishLog.weight != null) {
                                Divider(modifier = Modifier.padding(vertical = 4.dp))
                                InfoSection(
                                    title = "Vekt",
                                    value = String.format("%.1f kg", selectedFishLog.weight),
                                    titleStyle = MaterialTheme.typography.bodyMedium,
                                    valueStyle = MaterialTheme.typography.bodySmall
                                )
                            }

                            Divider(modifier = Modifier.padding(vertical = 4.dp))

                            InfoSection(
                                title = "Posisjon",
                                value = String.format("%.4f°N, %.4f°Ø", selectedFishLog.latitude, selectedFishLog.longitude),
                                titleStyle = MaterialTheme.typography.bodyMedium,
                                valueStyle = MaterialTheme.typography.bodySmall
                            )

                            Divider(modifier = Modifier.padding(vertical = 4.dp))

                            InfoSection(
                                title = "Dato",
                                value = dateFormat.format(selectedFishLog.timestamp),
                                titleStyle = MaterialTheme.typography.bodyMedium,
                                valueStyle = MaterialTheme.typography.bodySmall
                            )

                            selectedFishLog.imageUri?.let { uri ->
                                Divider(modifier = Modifier.padding(vertical = 4.dp))
                                Image(
                                    painter = rememberAsyncImagePainter(uri),
                                    contentDescription = "Fiskebilde",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                } else {
                    if (fishLogs.isEmpty()) {
                        Text(
                            "Ingen fiskelogger ennå",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    } else {
                        fishLogs.forEach { fishLog ->
                            var showDetails by remember { mutableStateOf(false) }
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDetails = !showDetails },
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    fishLog.fishType,
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                                if (fishLog.weight != null) {
                                                    Text(
                                                        String.format("%.1f kg", fishLog.weight),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }

                                        if (fishLog.imageUri != null) {
                                            Image(
                                                painter = rememberAsyncImagePainter(fishLog.imageUri),
                                                contentDescription = "Fiskebilde",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(75.dp),
                                                contentScale = ContentScale.Fit
                                            )
                                        }

                                        if (showDetails) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("Sted: ${fishLog.location}")
                                            Text("Område: ${fishLog.area}")
                                            if (fishLog.description.isNotBlank()) {
                                                Text("Beskrivelse: ${fishLog.description}")
                                            }
                                            Text("Dato: ${dateFormat.format(fishLog.timestamp)}")
                                        }
                                    }

                                    // X-knapp for å fjerne fisk
                                    IconButton(
                                        onClick = { onRemoveFish(fishLog) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Fjern fisk",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedFishLog == null && fishLogs.isNotEmpty()) {
                TextButton(onClick = onClearLogs) {
                    Text("Tøm logg")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Lukk")
            }
        }
    )
}

@Composable
private fun InfoSection(
    title: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    titleStyle: TextStyle = MaterialTheme.typography.titleMedium,
    valueStyle: TextStyle = MaterialTheme.typography.bodyLarge
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = titleStyle,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
        )
        Text(
            text = value,
            style = valueStyle,
            color = color.copy(alpha = 0.9f)
        )
    }
} 