package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.farevarsel

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.farevarsel.GeoJsonRepository
import org.json.JSONObject
import org.maplibre.android.geometry.LatLng
import java.util.Locale

private const val TAG = "GeoJsonViewModel"

// UI State for MapScreen
data class MapUiState(
    val geoJsonData: String? = null,
    val selectedAlert: JSONObject? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class GeoJsonViewModel(private val applicationContext: Context) : ViewModel() {
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

    // Søkefunksjonalitet
    private val _searchTarget = MutableStateFlow<LatLng?>(null)
    val searchTarget: StateFlow<LatLng?> = _searchTarget

    fun searchAndMoveToLocation(query: String) {
        viewModelScope.launch {
            val location = geocode(query)
            if (location != null) {
                _searchTarget.value = location
            } else {
                _uiState.update {
                    it.copy(error = "Fant ikke sted: $query")
                }
            }
        }
    }

    suspend fun geocode(query: String): LatLng? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(applicationContext, Locale.getDefault())
                val results = geocoder.getFromLocationName(query, 1)
                if (!results.isNullOrEmpty()) {
                    val result = results[0]
                    LatLng(result.latitude, result.longitude)
                } else {
                    Log.d(TAG, "No results for query: $query")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Geocoding error: ${e.message}", e)
                null
            }
        }
    }

    fun setSelectedAlert(alert: JSONObject?) {
        _uiState.update { it.copy(selectedAlert = alert) }
    }

    fun retry() {
        loadGeoJsonData()
    }
}