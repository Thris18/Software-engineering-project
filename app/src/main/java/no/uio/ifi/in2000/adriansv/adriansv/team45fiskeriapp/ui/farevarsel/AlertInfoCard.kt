package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.farevarsel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.json.JSONObject
import kotlin.let
import kotlin.takeIf
import kotlin.text.isNotEmpty

@Composable
fun AlertInfoCard(
    alertData: JSONObject?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (alertData == null) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Tittelrad med ikon og lukkeknapp
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = alertData.optString("eventAwarenessName", "Ukjent varsel"),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                // Viser alvorlighetsgrad
                val severityColor = when(alertData.optString("severity")) {
                    "Severe" -> Color(0xFFD32F2F)
                    "Moderate" -> Color(0xFFF57C00)
                    else -> Color(0xFFFBC02D)
                }
                Surface(
                    color = severityColor,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = when(alertData.optString("severity")) {
                            "Severe" -> "Høy fare"
                            "Moderate" -> "Moderat fare"
                            "Minor" -> "Lav fare"
                            else -> "Ukjent"
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                // Lukkeknappen
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Text("✕", color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Info om område
            Text(
                text = "Område",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = alertData.optString("area", "Ukjent område"),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Beskrivelse
            Text(
                text = "Beskrivelse",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = alertData.optString("description", "Ingen beskrivelse tilgjengelig"),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Anbefaling hvis tilgjengelig
            alertData.optString("recommendation", "").takeIf { it.isNotEmpty() }?.let { recommendation ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Anbefaling",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = recommendation,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}