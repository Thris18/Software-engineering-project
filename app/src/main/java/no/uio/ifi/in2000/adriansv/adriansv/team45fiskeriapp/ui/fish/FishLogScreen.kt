package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fish

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.fish.FishLog
import java.text.SimpleDateFormat
import java.util.*

private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

enum class SortOrder {
    FISH_TYPE_ASC,
    WEIGHT_DESC,
    DATE_DESC
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishLogScreen(
    fishLogs: List<FishLog>,
    onBackClick: () -> Unit,
    onAddFish: () -> Unit,
    onRemoveFish: (FishLog) -> Unit
) {
    var currentSortOrder by remember { mutableStateOf(SortOrder.DATE_DESC) }
    var showSortMenu by remember { mutableStateOf(false) }

    val sortedFishLogs = remember(fishLogs, currentSortOrder) {
        when (currentSortOrder) {
            SortOrder.FISH_TYPE_ASC -> fishLogs.sortedBy { it.fishType }
            SortOrder.WEIGHT_DESC -> fishLogs.sortedByDescending { it.weight ?: 0f }
            SortOrder.DATE_DESC -> fishLogs.sortedByDescending { it.timestamp }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mine fisker") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Gå tilbake"
                        )
                    }
                },
                actions = {
                    // Sorteringsknapp
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sorter"
                        )
                    }
                    // Legg til fisk-knapp
                    IconButton(onClick = onAddFish) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Legg til fisk"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (fishLogs.isEmpty()) {
                // Vis melding når det ikke er noen fisker
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Ingen fisker lagt til ennå",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onAddFish) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Legg til din første fisk")
                    }
                }
            } else {
                // Liste med fisker
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sortedFishLogs) { fishLog ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth()
                            ) {
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
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Venstre side med informasjon
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = fishLog.fishType,
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                                if (fishLog.weight != null) {
                                                    Text(
                                                        text = String.format("%.1f kg", fishLog.weight),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                Text(
                                                    text = dateFormat.format(fishLog.timestamp),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        // Vis alle detaljer
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            if (fishLog.location.isNotEmpty()) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.LocationOn,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp),
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = fishLog.location,
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                }
                                            }
                                            if (fishLog.area.isNotEmpty()) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Map,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp),
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = fishLog.area,
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                }
                                            }
                                            if (fishLog.description.isNotEmpty()) {
                                                Row(
                                                    verticalAlignment = Alignment.Top
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Info,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp),
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = fishLog.description,
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Høyre side med bilde
                                    if (fishLog.imageUri != null) {
                                        Box(
                                            modifier = Modifier
                                                .width(150.dp)
                                                .fillMaxHeight()
                                                .padding(end = 40.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AsyncImage(
                                                model = fishLog.imageUri,
                                                contentDescription = "Bilde av ${fishLog.fishType}",
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(4.dp),
                                                contentScale = ContentScale.Fit
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Sorteringsmeny
            if (showSortMenu) {
                AlertDialog(
                    onDismissRequest = { showSortMenu = false },
                    title = { Text("Sorter etter") },
                    text = {
                        Column {
                            TextButton(
                                onClick = {
                                    currentSortOrder = SortOrder.FISH_TYPE_ASC
                                    showSortMenu = false
                                }
                            ) {
                                Text("Fisketype (A-Å)")
                            }
                            TextButton(
                                onClick = {
                                    currentSortOrder = SortOrder.WEIGHT_DESC
                                    showSortMenu = false
                                }
                            ) {
                                Text("Vekt (høyest først)")
                            }
                            TextButton(
                                onClick = {
                                    currentSortOrder = SortOrder.DATE_DESC
                                    showSortMenu = false
                                }
                            ) {
                                Text("Dato (nyeste først)")
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showSortMenu = false }) {
                            Text("Lukk")
                        }
                    }
                )
            }
        }
    }
} 