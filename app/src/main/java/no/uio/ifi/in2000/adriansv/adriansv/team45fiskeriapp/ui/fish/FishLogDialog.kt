package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fish

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.fish.FishLog
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * FISKELOGG-DIALOG FOR FISKERIAPPLIKASJONEN
 * 
 * Denne filen implementerer dialoggrensesnittet for å vise, redigere og administrere
 * brukerens fiskelogg direkte fra kartvisningen.
 * 
 * Nøkkelfunksjonalitet:
 * - Visning av alle registrerte fiskefangster i en kompakt dialogform
 * - Detaljert visning av enkeltfangster når de velges på kartet
 * - Mulighet for å legge til nye fiskefangster
 * - Mulighet for å slette eksisterende fangster
 * - Visning av fangstbilder direkte i dialogen
 * - Mulighet for å tømme hele fiskeloggen
 * 
 * Filen inneholder:
 * - FishLogDialog: Hovedkomponenten som viser fiskeloggen i dialogformat
 * - InfoSection: Støttekomponent for å vise informasjon om fiskefangster
 * 
 * Dialogen gir brukeren rask tilgang til sin fiskelogg under aktive fisketurer
 * og gir en umiddelbar oversikt over fangster uten å måtte navigere bort fra kartet.
 * Dette er spesielt nyttig når brukeren ønsker å sjekke tidligere fangststeder mens
 * de planlegger hvor de skal fiske neste gang.
 */

@SuppressLint("ConstantLocale")
private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

@SuppressLint("DefaultLocale")
@Composable
fun FishLogDialog(
    fishLogs: List<FishLog>,
    onDismiss: () -> Unit,
    onClearLogs: () -> Unit,
    onAddFish: () -> Unit,
    selectedLocation: Pair<Double, Double>?,
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

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            InfoSection(
                                title = "Område",
                                value = selectedFishLog.area,
                                titleStyle = MaterialTheme.typography.bodyMedium,
                                valueStyle = MaterialTheme.typography.bodySmall
                            )

                            if (selectedFishLog.description.isNotBlank()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                InfoSection(
                                    title = "Beskrivelse",
                                    value = selectedFishLog.description,
                                    titleStyle = MaterialTheme.typography.bodyMedium,
                                    valueStyle = MaterialTheme.typography.bodySmall
                                )
                            }

                            if (selectedFishLog.weight != null) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                InfoSection(
                                    title = "Vekt",
                                    value = String.format("%.1f kg", selectedFishLog.weight),
                                    titleStyle = MaterialTheme.typography.bodyMedium,
                                    valueStyle = MaterialTheme.typography.bodySmall
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            InfoSection(
                                title = "Dato",
                                value = dateFormat.format(selectedFishLog.timestamp),
                                titleStyle = MaterialTheme.typography.bodyMedium,
                                valueStyle = MaterialTheme.typography.bodySmall
                            )

                            selectedFishLog.imageUri?.let { uri ->
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
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