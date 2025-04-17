package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.ship

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.ship.Ship
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@Composable
fun ShipInfoCard(
    ship: Ship,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            // Title
            Text(
                text = if (ship.name == "Ukjent") "Ukjent skip" else ship.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            
            // Subtitle (MMSI)
            Text(
                text = "MMSI: ${ship.mmsi}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Info rows
            InfoRow(
                label = "Type",
                value = ship.displayType
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            InfoRow(
                label = "Posisjon",
                value = String.format("%.4f°N, %.4f°Ø", ship.latitude, ship.longitude)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            InfoRow(
                label = "Fart",
                value = String.format("%.1f knop", ship.speed)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            InfoRow(
                label = "Kurs",
                value = String.format("%.1f°", ship.course)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            InfoRow(
                label = "Sist oppdatert",
                value = getRelativeTimeString(ship.messageTime)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Close button
            Button(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Lukk")
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun getRelativeTimeString(messageTime: String): String {
    try {
        // Parse UTC time with timezone offset
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        val parsedDate = inputFormat.parse(messageTime) ?: return "Ukjent tid"
        
        // Convert to Norwegian time
        val norwegianTimeZone = TimeZone.getTimeZone("Europe/Oslo")
        val now = Calendar.getInstance(norwegianTimeZone)
        val messageDate = Calendar.getInstance(norwegianTimeZone).apply { 
            time = parsedDate 
        }
        
        val diffInMillis = now.timeInMillis - messageDate.timeInMillis
        val diffInMinutes = diffInMillis / (60 * 1000)
        val diffInHours = diffInMinutes / 60
        
        return when {
            diffInMinutes < 1 -> "nå"
            diffInMinutes < 60 -> "${diffInMinutes.toInt()} min siden"
            diffInHours < 24 -> "${diffInHours.toInt()} timer siden"
            else -> {
                SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).apply {
                    timeZone = norwegianTimeZone
                }.format(messageDate.time)
            }
        }
    } catch (e: Exception) {
        // Hvis første forsøk feiler, prøv å parse med mikrosekunder
        try {
            val inputFormatWithMicros = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX", Locale.getDefault())
            val parsedDate = inputFormatWithMicros.parse(messageTime) ?: return "Ukjent tid"
            
            val norwegianTimeZone = TimeZone.getTimeZone("Europe/Oslo")
            val now = Calendar.getInstance(norwegianTimeZone)
            val messageDate = Calendar.getInstance(norwegianTimeZone).apply { 
                time = parsedDate 
            }
            
            val diffInMillis = now.timeInMillis - messageDate.timeInMillis
            val diffInMinutes = diffInMillis / (60 * 1000)
            val diffInHours = diffInMinutes / 60
            
            return when {
                diffInMinutes < 1 -> "nå"
                diffInMinutes < 60 -> "${diffInMinutes.toInt()} min siden"
                diffInHours < 24 -> "${diffInHours.toInt()} timer siden"
                else -> {
                    SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).apply {
                        timeZone = norwegianTimeZone
                    }.format(messageDate.time)
                }
            }
        } catch (e: Exception) {
            return "Ukjent tid"
        }
    }
} 