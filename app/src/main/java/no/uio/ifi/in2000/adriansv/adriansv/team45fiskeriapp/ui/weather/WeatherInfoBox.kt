package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.weather

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather.LocationWeather
import java.util.*

private const val TAG = "WeatherInfoBox"

fun isDay(): Int {
    val calendar = Calendar.getInstance()
    return calendar.get(Calendar.HOUR_OF_DAY)
}

@Composable
fun WeatherInfoBox(
    weather: LocationWeather,
    weatherState: WeatherUiState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Temperatur
            Text(
                text = "${weather.temperature.toInt()}°C",
                fontSize = 16.sp,
                color = Color.Black
            )
            
            // Værikon - Load SVG directly from assets
            val context = LocalContext.current
            val svgString = remember(weather.symbolCode) {
                try {
                    // Legg til "svg/" i banen
                    val iconPath = weather.symbolCode.replace("symboler/lightmode/", "symbols/lightmode/svg/")
                    val rawSvg = context.assets.open(iconPath).bufferedReader().use { it.readText() }
                    """
                    <html>
                        <head>
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <style>
                                body {
                                    margin: 0;
                                    padding: 0;
                                    display: flex;
                                    justify-content: center;
                                    align-items: center;
                                    background-color: transparent;
                                    width: 24px;
                                    height: 24px;
                                }
                                svg {
                                    width: 100%;
                                    height: 100%;
                                }
                            </style>
                        </head>
                        <body>
                            $rawSvg
                        </body>
                    </html>
                    """.trimIndent()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load weather icon: ${weather.symbolCode}", e)
                    null
                }
            }
            
            if (svgString != null) {
                AndroidView(
                    factory = { ctx ->
                        android.webkit.WebView(ctx).apply {
                            setBackgroundColor(android.graphics.Color.TRANSPARENT)
                            settings.javaScriptEnabled = false
                            settings.useWideViewPort = false
                            settings.loadWithOverviewMode = true
                        }
                    },
                    update = { webView ->
                        webView.loadDataWithBaseURL(
                            null,
                            svgString,
                            "text/html",
                            "UTF-8",
                            null
                        )
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
} 