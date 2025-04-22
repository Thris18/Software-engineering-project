package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.map

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.R

@Composable
fun SettingsMenu(
    showGrib: Boolean,
    showAlerts: Boolean,
    showShips: Boolean,
    onGribFilterChanged: (Boolean) -> Unit,
    onAlertsFilterChanged: (Boolean) -> Unit,
    onShipsFilterChanged: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var showFilterOptions by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showFilterOptions = false
    }

    Popup(
        alignment = Alignment.TopEnd,
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .padding(end = 16.dp, top = 72.dp)
                .width(200.dp),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth()
            ) {
                ListItem(
                    headlineContent = { Text("Filtrer") },
                    trailingContent = {
                        IconButton(onClick = { showFilterOptions = !showFilterOptions }) {
                            Icon(
                                imageVector = if (showFilterOptions) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (showFilterOptions) "Skjul filtervalg" else "Vis filtervalg"
                            )
                        }
                    },
                    modifier = Modifier.clickable { showFilterOptions = !showFilterOptions }
                )

                if (showFilterOptions) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth()
                    ) {
                        // GRIB data toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("GRIB data")
                            Switch(
                                checked = showGrib,
                                onCheckedChange = onGribFilterChanged,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Farevarsler toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Farevarsler")
                            Switch(
                                checked = showAlerts,
                                onCheckedChange = onAlertsFilterChanged,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Skip toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Skip")
                            Switch(
                                checked = showShips,
                                onCheckedChange = onShipsFilterChanged,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                }

                // Her kan du legge til flere menyvalg senere
                // For eksempel:
                // ListItem(
                //     headlineContent = { Text("Annet valg") }
                // )
            }
        }
    }
} 