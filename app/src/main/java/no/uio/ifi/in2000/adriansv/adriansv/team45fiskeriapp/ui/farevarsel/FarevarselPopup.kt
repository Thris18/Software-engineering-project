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

@Composable
fun FarevarselPopup(
    alertData: JSONObject?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (alertData == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = alertData.optString("eventAwarenessName").removeSurrounding("\""),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                // Viser alvorlighetsgrad
                val severity = alertData.optString("severity").removeSurrounding("\"")
                val severityColor = when(severity) {
                    "Severe" -> Color(0xFFD32F2F)
                    "Moderate" -> Color(0xFFF57C00)
                    "Minor" -> Color(0xFFFBC02D)
                    else -> Color(0xFFFBC02D)
                }
                Surface(
                    color = severityColor,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = when(severity) {
                            "Severe" -> "Høy fare"
                            "Moderate" -> "Moderat fare"
                            "Minor" -> "Lav fare"
                            else -> severity
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Info om område
                Text(
                    text = "Område",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = alertData.optString("area").removeSurrounding("\""),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Beskrivelse
                Text(
                    text = "Beskrivelse",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = alertData.optString("description").removeSurrounding("\""),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Anbefaling hvis tilgjengelig
                val recommendation = alertData.optString("recommendation").removeSurrounding("\"")
                if (recommendation.isNotEmpty()) {
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
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Lukk")
            }
        }
    )
}