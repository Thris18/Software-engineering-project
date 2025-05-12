package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.input.KeyboardType
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib.GribOverlayUtil
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Vindhastighet:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedTextField(
                                    value = windText,
                                    onValueChange = {
                                        val filtered = it.replace(',', '.')
                                        windText = filtered
                                        filtered.toFloatOrNull()?.let { value ->
                                            if (value in 0f..30f) {
                                                windThreshold = String.format("%.2f", value).toFloat()
                                                windText = String.format("%.2f", value)
                                            }
                                        }
                                    },
                                    modifier = Modifier.width(70.dp),
                                    textStyle = MaterialTheme.typography.bodyMedium,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "m/s",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Slider(
                                value = windThreshold,
                                onValueChange = {
                                    val rounded = String.format("%.2f", it).toFloat()
                                    windThreshold = rounded
                                    windText = String.format("%.2f", rounded)
                                },
                                valueRange = 0f..30f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Strømhastighet:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedTextField(
                                    value = currentText,
                                    onValueChange = {
                                        val filtered = it.replace(',', '.')
                                        currentText = filtered
                                        filtered.toFloatOrNull()?.let { value ->
                                            if (value in 0f..5f) {
                                                currentThreshold = String.format("%.2f", value).toFloat()
                                                currentText = String.format("%.2f", value)
                                            }
                                        }
                                    },
                                    modifier = Modifier.width(70.dp),
                                    textStyle = MaterialTheme.typography.bodyMedium,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "m/s",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Slider(
                                value = currentThreshold,
                                onValueChange = {
                                    val rounded = String.format("%.2f", it).toFloat()
                                    currentThreshold = rounded
                                    currentText = String.format("%.2f", rounded)
                                },
                                valueRange = 0f..5f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Nedbør:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedTextField(
                                    value = precipitationText,
                                    onValueChange = {
                                        val filtered = it.replace(',', '.')
                                        precipitationText = filtered
                                        filtered.toFloatOrNull()?.let { value ->
                                            if (value in 0f..20f) {
                                                precipitationThreshold = String.format("%.2f", value).toFloat()
                                                precipitationText = String.format("%.2f", value)
                                            }
                                        }
                                    },
                                    modifier = Modifier.width(70.dp),
                                    textStyle = MaterialTheme.typography.bodyMedium,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "mm/time",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Slider(
                                value = precipitationThreshold,
                                onValueChange = {
                                    val rounded = String.format("%.2f", it).toFloat()
                                    precipitationThreshold = rounded
                                    precipitationText = String.format("%.2f", rounded)
                                },
                                valueRange = 0f..20f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Bølgehøyde:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedTextField(
                                    value = waveHeightText,
                                    onValueChange = {
                                        val filtered = it.replace(',', '.')
                                        waveHeightText = filtered
                                        filtered.toFloatOrNull()?.let { value ->
                                            if (value in 0f..10f) {
                                                waveHeightThreshold = String.format("%.2f", value).toFloat()
                                                waveHeightText = String.format("%.2f", value)
                                            }
                                        }
                                    },
                                    modifier = Modifier.width(70.dp),
                                    textStyle = MaterialTheme.typography.bodyMedium,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "m",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Slider(
                                value = waveHeightThreshold,
                                onValueChange = {
                                    val rounded = String.format("%.2f", it).toFloat()
                                    waveHeightThreshold = rounded
                                    waveHeightText = String.format("%.2f", rounded)
                                },
                                valueRange = 0f..10f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
} 