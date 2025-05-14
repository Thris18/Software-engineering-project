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

    // Henter banen til SVG-filen for det spesifikke ikonet
    val iconPath = weatherIcon.getAssetPath()

    val svgString = remember(iconPath) {
        try {

            // Leser inn SVG-filen fra appens ressurser
            val rawSvg = context.assets.open(iconPath).bufferedReader().use { it.readText() }
            """
            <html>
                <body style="margin:0;padding:0;">
                    <div style="width:${size.value}px;height:${size.value}px;">$rawSvg</div>
                </body>
            </html>
            """.trimIndent()
        } catch (_: Exception) {

            // Hvis det oppstår en feil, vises feilmelding
            "<html><body>Ikon mangler</body></html>"
        }
    }

    // Bruker AndroidView for å vise WebView som laster SVG-strengen
    AndroidView(
        factory = {

            // Setter opp WebView for å vise SVG-data som HTML
            WebView(it).apply {

                // Aktiverer JavaScript i WebView for korrekt visning av SVG
                settings.javaScriptEnabled = true

                // Gjør WebView bakgrunnen gjennomsiktig
                setBackgroundColor(0x00000000)

                // Laster SVG-strengen som HTML innhold i WebView
                loadDataWithBaseURL(null, svgString, "text/html", "utf-8", null)
            }
        },

        //Setter størrelsen på WebView for å matche ønsket størrelse på ikonet
        modifier = Modifier.size(size)
    )
}
