package no.uio.ifi.in2000.carlorr.btapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.farevarsel.MapLibreInitializer
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.navigation.MainNavigation
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.theme.OslofjordGribAppTheme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // OSMDroid
        Configuration.getInstance().userAgentValue = applicationContext.packageName

        // MapLibre
        MapLibreInitializer.initialize(applicationContext)

        setContent {
            OslofjordGribAppTheme {
                MainNavigation()
            }
        }
    }
}