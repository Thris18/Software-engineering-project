package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fish

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.fish.FishLog
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * FISKELOGG-SKJERMEN FOR FISKERIAPPLIKASJONEN
 * 
 * Denne filen implementerer hovedvisningen for brukerens fiskelogg, hvor
 * alle registrerte fangster vises og kan administreres.
 * 
 * Nøkkelfunksjonalitet:
 * - Visning av alle registrerte fiskefangster i en scrollbar liste
 * - Sortering av fangster etter fisketype, vekt eller dato
 * - Detaljert visning av hver fangst med bilde, vekt, område og tidspunkt
 * - Mulighet for å legge til nye fangster
 * - Mulighet for å slette eksisterende fangster
 * - Visning av bilder i fullskjerm ved klikk
 * - Tomtilstandshåndtering når ingen fangster er registrert
 * 
 * Filen inneholder:
 * - SortOrder: Enum for ulike sorteringsalternativer
 * - FishLogScreen: Hovedkomponenten som viser fiskeloggen
 * - Støttekomponenter for sortering og bildehåndtering
 * 
 * Skjermen er en sentral del av applikasjonens fiskeloggsfunksjonalitet,
 * og lar fiskeren holde oversikt over og dokumentere sine fangster over tid.
 */

@SuppressLint("ConstantLocale")
private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

enum class SortOrder {
    FISH_TYPE_ASC,
    WEIGHT_DESC,
    DATE_DESC
}

@SuppressLint("DefaultLocale")
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
    var selectedImageUri by remember { mutableStateOf<String?>(null) }

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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Gå tilbake"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sorter"
                        )
                    }
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
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
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
                                        Text(
                                            text = dateFormat.format(fishLog.timestamp),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    if (fishLog.imageUri != null) {
                                        Box(
                                            modifier = Modifier
                                                .width(120.dp)
                                                .height(120.dp)
                                                .padding(end = 40.dp)
                                                .clickable { selectedImageUri = fishLog.imageUri },
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

            if (selectedImageUri != null) {
                Dialog(onDismissRequest = { selectedImageUri = null }) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Stor visning av bilde",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 600.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Fit
                            )
                            IconButton(
                                onClick = { selectedImageUri = null },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Lukk", tint = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
} 