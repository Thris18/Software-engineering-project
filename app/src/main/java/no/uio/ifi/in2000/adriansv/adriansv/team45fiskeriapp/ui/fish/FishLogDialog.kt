package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fish

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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

@Composable
fun FishLogDialog(
    fishLogs: List<FishLog>,
    onDismiss: () -> Unit,
    onClearLogs: () -> Unit,
    onAddFish: () -> Unit,
    selectedLocation: Pair<Double, Double>? = null,
    failedImageLoads: Set<String> = emptySet(),
    onImageLoadError: (String) -> Unit = {}
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
                modifier = Modifier.fillMaxWidth()
            ) {
                if (selectedFishLog != null) {
                    FishLogItem(
                        fishLog = selectedFishLog,
                        showAllDetails = true,
                        failedImageLoads = failedImageLoads,
                        onImageLoadError = onImageLoadError
                    )
                } else if (fishLogs.isEmpty()) {
                    Text(
                        "Ingen fisker logget ennå",
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                    ) {
                        items(fishLogs) { fishLog ->
                            FishLogItem(
                                fishLog = fishLog,
                                failedImageLoads = failedImageLoads,
                                onImageLoadError = onImageLoadError
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (selectedFishLog == null) {
                    TextButton(
                        onClick = onClearLogs,
                        enabled = fishLogs.isNotEmpty()
                    ) {
                        Text("Tøm logg")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Lukk")
                }
            }
        }
    )
}

@Composable
private fun FishLogItem(
    fishLog: FishLog,
    showAllDetails: Boolean = false,
    failedImageLoads: Set<String> = emptySet(),
    onImageLoadError: (String) -> Unit = {}
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    var showDetails by remember { mutableStateOf(showAllDetails) }
    val imageId = "fish_${fishLog.timestamp}"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(enabled = !showAllDetails) { showDetails = !showDetails },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fishLog.fishType,
                    style = MaterialTheme.typography.titleMedium
                )
                if (fishLog.weight != null) {
                    Text("${fishLog.weight} kg")
                }
            }
            
            if (fishLog.imageUri != null && imageId !in failedImageLoads) {
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = rememberAsyncImagePainter(
                        model = Uri.parse(fishLog.imageUri),
                        onError = { onImageLoadError(imageId) }
                    ),
                    contentDescription = "Bilde av ${fishLog.fishType}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
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
    }
} 