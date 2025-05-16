package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.core.utils.MapLibreInitializer
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.navigation.NavigationHandler
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.theme.Team45FiskeriAppTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize MapLibre
        MapLibreInitializer.initialize(applicationContext)
        
        setContent {
            Team45FiskeriAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavigationHandler()
                }
            }
        }
    }
}