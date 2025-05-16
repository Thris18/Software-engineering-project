package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.alerts

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * FAREVARSEL-POPUP FOR FISKERIAPPLIKASJONEN
 * 
 * Denne filen implementerer en interaktiv popup-komponent som viser detaljer 
 * om farevarsler til sjøs fra Meteorologisk institutt, slik at brukerne kan 
 * ta informerte avgjørelser om sine fisketurer.
 * 
 * Nøkkelfunksjonalitet:
 * - Presentasjon av viktige farevarsler i en elegant, flyttbar popup
 * - Konvertering av UTC-tidspunkter til norsk tid for bedre forståelse
 * - Visuell indikasjon av faregrad (høy, moderat, lav) med farger
 * - Pulserende effekt for høyrisiko-varsler for å tiltrekke oppmerksomhet
 * - Interaktivitet med drag-funksjonalitet for å flytte varselet rundt på skjermen
 * - Detaljert presentasjon av området, beskrivelsen og anbefalinger
 * 
 * Filen inneholder:
 * - Hjelpefunksjoner for tidskonvertering fra UTC til norsk tid
 * - FarevarselPopup: Hovedkomponenten som viser farevarsel-informasjon
 * - Animasjonslogikk for vising, lukking og interaksjon med popup-en
 * 
 * Denne komponenten utgjør en kritisk sikkerhetsfeature i appen, som 
 * hjelper fiskere med å unngå farlige værforhold og ta forholdsregler
 * basert på offisielle meteorologiske varsler.
 */

private fun convertUTCtoNorwegianTime(utcTimeString: String): String {
    try {
        val cleaned = utcTimeString
            .replace(Regex("\\bkl\\b", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\bUTC\\b", RegexOption.IGNORE_CASE), "")
            .trim()

        val parts = cleaned.split(" ")
        if (parts.size < 3) return utcTimeString

        val day = parts[0].toIntOrNull() ?: return utcTimeString
        val month = when (parts[1].lowercase(Locale.getDefault())) {
            "januar" -> Calendar.JANUARY
            "februar" -> Calendar.FEBRUARY
            "mars" -> Calendar.MARCH
            "april" -> Calendar.APRIL
            "mai" -> Calendar.MAY
            "juni" -> Calendar.JUNE
            "juli" -> Calendar.JULY
            "august" -> Calendar.AUGUST
            "september" -> Calendar.SEPTEMBER
            "oktober" -> Calendar.OCTOBER
            "november" -> Calendar.NOVEMBER
            "desember" -> Calendar.DECEMBER
            else -> return utcTimeString
        }
        val timeParts = parts[2].split(":")
        if (timeParts.size != 2) return utcTimeString
        val hour = timeParts[0].toIntOrNull() ?: return utcTimeString
        val minute = timeParts[1].toIntOrNull() ?: return utcTimeString

        // Build UTC Calendar
        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.MONTH, month)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }

        // Format to Norwegian time
        val formatter = SimpleDateFormat("d. MMMM HH:mm", Locale("nb","NO"))
        formatter.timeZone = TimeZone.getTimeZone("Europe/Oslo")
        return formatter.format(utcCal.time)
    } catch (e: Exception) {
        return utcTimeString
    }
}

private fun convertDescriptionToNorwegianTime(description: String): String {
    // Remove all kl and UTC
    val noUtc = description
        .replace(Regex("\\bkl\\b", RegexOption.IGNORE_CASE), "")
        .replace(Regex("\\bUTC\\b", RegexOption.IGNORE_CASE), "")
    val parts = noUtc.split(" fra ", " til ")
    if (parts.size < 3) return noUtc

    val base = parts[0]
    val start = convertUTCtoNorwegianTime(parts[1])
    val endPart = parts[2]
    val (end, extra) = endPart.split(" og ", limit = 2).let {
        it[0] to it.getOrNull(1)
    }

    val endTime = convertUTCtoNorwegianTime(end)
    val secondAlert = extra
        ?.let { " og ${convertDescriptionToNorwegianTime(it)}" }
        .orEmpty()

    return "$base fra $start til $endTime$secondAlert"
}

@Composable
fun FarevarselPopup(
    alertData: JSONObject?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (alertData == null) return

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(alertData) {
        isVisible = true
    }

    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    val animatedOffsetY by animateFloatAsState(
        targetValue = offsetY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(
            initialAlpha = 0f,
            animationSpec = tween(300)
        ) + expandIn(
            expandFrom = Alignment.Center,
            animationSpec = tween(300)
        ),
        exit = fadeOut(
            animationSpec = tween(200)
        ) + shrinkOut(
            shrinkTowards = Alignment.Center,
            animationSpec = tween(200)
        )
    ) {
        Surface(
            modifier = modifier
                .offset { IntOffset(animatedOffsetX.roundToInt(), animatedOffsetY.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
                .graphicsLayer {
                    rotationZ = (offsetX * 0.02f).coerceIn(-3f, 3f)
                },
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.90f)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .widthIn(max = 260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header with close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Tittel og severity badge i en kolonne
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = alertData.optString("eventAwarenessName"),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.95f)
                            )
                            
                            // Severity badge with animated alpha
                            val severity = alertData.optString("severity")
                            val severityColor = when(severity) {
                                "Severe" -> Color(0xFFD32F2F)
                                "Moderate" -> Color(0xFFF57C00)
                                "Minor" -> Color(0xFFFBC02D)
                                else -> Color(0xFFCCCCCC)
                            }
                            
                            val isHighSeverity = severity == "Severe"
                            val alpha by animateFloatAsState(
                                targetValue = if (isHighSeverity) 0.8f else 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000),
                                    repeatMode = RepeatMode.Reverse
                                )
                            )
                            
                            Surface(
                                color = severityColor.copy(alpha = if (isHighSeverity) alpha else 1f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.wrapContentWidth()
                            ) {
                                Text(
                                    text = when(severity) {
                                        "Severe" -> "Høy fare"
                                        "Moderate" -> "Moderat fare"
                                        "Minor" -> "Lav fare"
                                        else -> severity
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                        
                        IconButton(
                            onClick = {
                                isVisible = false
                                kotlinx.coroutines.MainScope().launch {
                                    delay(300)
                                    onDismiss()
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Lukk popup",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "Område",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                            )
                            Text(
                                alertData.optString("area"),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "Beskrivelse",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                            )
                            Text(
                                convertDescriptionToNorwegianTime(alertData.optString("description")),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "Anbefaling",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                            )
                            Text(
                                alertData.optString("recommendation").takeIf { it.isNotBlank() }
                                    ?: "Følg med på værmeldinger og vær forberedt på endringer i værforholdene.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }
    }
}