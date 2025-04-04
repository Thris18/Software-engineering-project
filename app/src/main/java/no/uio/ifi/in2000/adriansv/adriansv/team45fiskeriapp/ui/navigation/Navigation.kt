package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.farevarsel.MapScreen
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.oslogrib.OslofjordGribScreen

private const val TAG = "Navigation"

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val currentRoute by navController.currentBackStackEntryAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Warning, contentDescription = "Farevarsler") },
                    label = { Text("Farevarsler") },
                    selected = currentRoute?.destination?.route == "alertMap",
                    onClick = { safeNavigate(navController, "alertMap") }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = "Værdata") },
                    label = { Text("Værdata") },
                    selected = currentRoute?.destination?.route == "oslofjordGrib",
                    onClick = { safeNavigate(navController, "oslofjordGrib") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "alertMap",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("oslofjordGrib") {
                OslofjordGribScreen()
            }
            composable("alertMap") {
                MapScreen()
            }
        }
    }
}

private fun safeNavigate(navController: NavController, route: String) {
    try {
        if (navController.currentDestination?.route != route) {
            navController.navigate(route)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Navigation error: ${e.message}", e)
    }
}