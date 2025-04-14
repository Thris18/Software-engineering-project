package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.farevarsel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.farevarsel.GeoJsonRepository
import org.json.JSONObject

private const val TAG = "GeoJsonViewModel"

// UI State for MapScreen
data class MapUiState(
    val geoJsonData: String? = null,
    val selectedAlert: JSONObject? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class GeoJsonViewModel : ViewModel() {
    private val repository = GeoJsonRepository.GeoJsonRepositoryImpl()

    private val _uiState = MutableStateFlow(MapUiState(isLoading = true))
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        loadGeoJsonData()
    }

    private fun loadGeoJsonData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.fetchGeoJson().fold(
                onSuccess = { data ->
                    _uiState.update {
                        it.copy(
                            geoJsonData = data,
                            isLoading = false,
                            error = null
                        )
                    }
                },
                onFailure = { error ->
                    Log.e(TAG, "Error loading GeoJSON data", error)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Error loading weather alerts: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    fun setSelectedAlert(alert: JSONObject?) {
        _uiState.update { it.copy(selectedAlert = alert) }
    }

    fun retry() {
        loadGeoJsonData()
    }
}