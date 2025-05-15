package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.weather

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.size
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather.WeatherIcon

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WeatherIconView(weatherIcon: WeatherIcon, size: Dp) {

    val context = LocalContext.current

    val iconPath = weatherIcon.getAssetPath()

    val svgString = remember(iconPath) {
        try {

            val rawSvg = context.assets.open(iconPath).bufferedReader().use { it.readText() }
            """
            <html>
                <body style="margin:0;padding:0;">
                    <div style="width:${size.value}px;height:${size.value}px;">$rawSvg</div>
                </body>
            </html>
            """.trimIndent()
        } catch (_: Exception) {

            "<html><body>Ikon mangler</body></html>"
        }
    }
    AndroidView(
        factory = {

            WebView(it).apply {

                settings.javaScriptEnabled = true

                setBackgroundColor(0x00000000)

                loadDataWithBaseURL(null, svgString, "text/html", "utf-8", null)
            }
        },

        modifier = Modifier.size(size)
    )
}
