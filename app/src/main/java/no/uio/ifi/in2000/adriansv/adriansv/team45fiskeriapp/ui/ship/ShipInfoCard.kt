package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.ship

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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.ship.Ship
import org.maplibre.android.geometry.LatLng
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

private fun getRelativeTimeString(messageTime: String): String {
    val formats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX"
    )
    var parsedDate: Date? = null

    for (pattern in formats) {
        try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            parsedDate = sdf.parse(messageTime)
            if (parsedDate != null) break
        } catch (_: Exception) { /* prøv neste mønster */ }
    }

    if (parsedDate == null) return messageTime  // kunne ikke parse

    // Konverter til norsk tid
    val norwegianCal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo")).apply {
        time = parsedDate
    }
    val now = Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo")).timeInMillis
    val diff = now - norwegianCal.timeInMillis

    val minutes = diff / (60 * 1000)
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1        -> "nå"
        minutes < 60       -> "$minutes min siden"
        hours < 24         -> "$hours t siden"
        days < 7           -> "$days dager siden"
        else -> {
            // Etter en uke viser vi eksakt dato
            SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("nb","NO")).format(norwegianCal.time)
        }
    }
}


@Composable
fun ShipInfoCard(
    ship: Ship,
    onDismiss: () -> Unit,
    shipScreenPosition: android.graphics.PointF?,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    
    // Sett offset direkte fra skipets posisjon
    var offsetX by remember(shipScreenPosition) { 
        mutableStateOf(shipScreenPosition?.x ?: 0f) 
    }
    var offsetY by remember(shipScreenPosition) { 
        mutableStateOf(shipScreenPosition?.y ?: 0f) 
    }
    
    // Animerte verdier for smooth bevegelse
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

    // Start animasjon når kortet vises
    LaunchedEffect(Unit) {
        delay(50) // Kort forsinkelse for å la layouten stabilisere seg
        isVisible = true
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(initialAlpha = 0f),
        exit = fadeOut()
    ) {
        Surface(
            modifier = modifier
                .offset { 
                    IntOffset(
                        (animatedOffsetX - (300f * density.density / 2)).roundToInt(),
                        (animatedOffsetY - (400f * density.density)).roundToInt()
                    ) 
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
                .graphicsLayer {
                    rotationZ = ((offsetX - (shipScreenPosition?.x ?: 0f)) * 0.02f).coerceIn(-3f, 3f)
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
                        .widthIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header med navn og lukkeknapp
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (ship.name == "Ukjent") "Ukjent skip" else ship.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.95f),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "MMSI: ${ship.mmsi}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
                                contentDescription = "Lukk",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Divider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )

                    // Info seksjoner
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        InfoSection(
                            title = "Fartøytype",
                            value = ship.displayType,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        InfoSection(
                            title = "Posisjon",
                            value = String.format("%.4f°N, %.4f°Ø", ship.latitude, ship.longitude)
                        )
                        
                        InfoSection(
                            title = "Fart",
                            value = String.format("%.1f knop", ship.speed)
                        )
                        
                        InfoSection(
                            title = "Kurs",
                            value = String.format("%.1f°", ship.course)
                        )
                        
                        InfoSection(
                            title = "Sist oppdatert",
                            value = getRelativeTimeString(ship.messageTime)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoSection(
    title: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = color.copy(alpha = 0.9f)
        )
    }
} 