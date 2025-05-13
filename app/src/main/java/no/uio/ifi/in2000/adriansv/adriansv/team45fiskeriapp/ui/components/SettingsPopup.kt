package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.input.KeyboardType
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib.utils.GribOverlayUtil
import android.content.Context

@Composable
fun SettingsPopup(
    context: Context,
    isDarkMode: Boolean,
    showGrib: Boolean,
    showAlerts: Boolean,
    showShips: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onGribFilterChanged: (Boolean) -> Unit,
    onAlertsFilterChanged: (Boolean) -> Unit,
    onShipsFilterChanged: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    // Hent lagrede terskelverdier
    val prefs = context.getSharedPreferences("GribThresholds", Context.MODE_PRIVATE)
    
    // Terskelverdier for varsler
    var windThreshold by remember { mutableStateOf(prefs.getFloat("wind_threshold", 10f)) }
    var currentThreshold by remember { mutableStateOf(prefs.getFloat("current_threshold", 2f)) }
    var precipitationThreshold by remember { mutableStateOf(prefs.getFloat("rain_threshold", 5f)) }
    var waveHeightThreshold by remember { mutableStateOf(prefs.getFloat("wave_threshold", 2f)) }

    // Tekstfelt for manuell input
    var windText by remember { mutableStateOf(windThreshold.toString()) }
    var currentText by remember { mutableStateOf(currentThreshold.toString()) }
    var precipitationText by remember { mutableStateOf(precipitationThreshold.toString()) }
    var waveHeightText by remember { mutableStateOf(waveHeightThreshold.toString()) }

    // Oppdater GribOverlayUtil når terskelverdiene endres
    LaunchedEffect(windThreshold, currentThreshold, precipitationThreshold, waveHeightThreshold) {
        GribOverlayUtil.updateThresholds(
            context = context,
            windThreshold = windThreshold,
            waveThreshold = waveHeightThreshold,
            currentThreshold = currentThreshold,
            precipitationThreshold = precipitationThreshold
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Innstillinger",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Lukk",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                HorizontalDivider()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Bytt modus",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 20.dp, top = 15.dp)
                    )
                    // Dark Mode Switch
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Dark Mode",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = onDarkModeChange,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Kart-Filter",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // GRIB data filter
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Værvarsler",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Switch(
                                checked = showGrib,
                                onCheckedChange = onGribFilterChanged,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Alerts filter
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Farevarsler",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Switch(
                                checked = showAlerts,
                                onCheckedChange = onAlertsFilterChanged,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Ships filter
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Skip",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Switch(
                                checked = showShips,
                                onCheckedChange = onShipsFilterChanged,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Terskelverdier for varsler",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Vindhastighet slider
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Text(
                                text = "Vindhastighet:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Slider(
                                    value = windThreshold,
                                    onValueChange = {
                                        val rounded = (Math.round(it * 10) / 10f)
                                        windThreshold = rounded
                                    },
                                    valueRange = 0f..10f,
                                    steps = 99,
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                                OutlinedTextField(
                                    value = String.format("%.1f", windThreshold),
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Verdi") },
                                    modifier = Modifier
                                        .width(80.dp)
                                        .padding(start = 8.dp),
                                    singleLine = true,
                                    enabled = false
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Strømhastighet slider
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Text(
                                text = "Strømhastighet:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Slider(
                                    value = currentThreshold,
                                    onValueChange = {
                                        val rounded = (Math.round(it * 100) / 100f)
                                        currentThreshold = rounded
                                    },
                                    valueRange = 0f..5f,
                                    steps = 499,
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                                OutlinedTextField(
                                    value = String.format("%.2f", currentThreshold),
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Verdi") },
                                    modifier = Modifier
                                        .width(80.dp)
                                        .padding(start = 8.dp),
                                    singleLine = true,
                                    enabled = false
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Nedbør slider
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Text(
                                text = "Nedbør:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Slider(
                                    value = precipitationThreshold,
                                    onValueChange = {
                                        val rounded = (Math.round(it * 100) / 100f)
                                        precipitationThreshold = rounded
                                    },
                                    valueRange = 0f..3f,
                                    steps = 299,
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                                OutlinedTextField(
                                    value = String.format("%.2f", precipitationThreshold),
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Verdi") },
                                    modifier = Modifier
                                        .width(80.dp)
                                        .padding(start = 8.dp),
                                    singleLine = true,
                                    enabled = false
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Bølgehøyde slider
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Text(
                                text = "Bølgehøyde:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Slider(
                                    value = waveHeightThreshold,
                                    onValueChange = {
                                        val rounded = (Math.round(it * 100) / 100f)
                                        waveHeightThreshold = rounded
                                    },
                                    valueRange = 0f..5f,
                                    steps = 499,
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                                OutlinedTextField(
                                    value = String.format("%.2f", waveHeightThreshold),
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Verdi") },
                                    modifier = Modifier
                                        .width(80.dp)
                                        .padding(start = 8.dp),
                                    singleLine = true,
                                    enabled = false
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
} 