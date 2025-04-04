package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.farevarsel

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.theme.Team45FiskeriAppTheme

@Composable
fun MapScreen(
    viewModel: GeoJsonViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Team45FiskeriAppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }

            if (uiState.geoJsonData != null) {
                MapView(
                    geoJsonData = uiState.geoJsonData,
                    onAlertSelected = { alertData ->
                        viewModel.setSelectedAlert(alertData)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            uiState.selectedAlert?.let { alert ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    AlertInfoCard(
                        alertData = alert,
                        onDismiss = { viewModel.setSelectedAlert(null) }
                    )
                }
            }
        }
    }
}