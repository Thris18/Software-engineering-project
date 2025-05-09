package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.grib.GribRepository
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style

private const val TAG = "GribViewModel"

data class GribUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val geoJson: String? = null
)

class GribViewModel(
    private val context: Context,
    private val gribRepository: GribRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GribUiState())
    val uiState: StateFlow<GribUiState> = _uiState.asStateFlow()

    fun loadGribOverlay(mapLibreMap: MapLibreMap?) {
        if (mapLibreMap == null) return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val allGrids = gribRepository.getAllWeatherGrids()
                val geoJson = GribOverlayUtil.mergeFeatureCollections(
                    allGrids["wind"]?.let { GribOverlayUtil.gribDataToFeatureCollection(it, "wind", "wind") } ?: "",
                    allGrids["wave"]?.let { GribOverlayUtil.gribDataToFeatureCollection(it, "wave", "wave") } ?: "",
                    allGrids["strom"]?.let { GribOverlayUtil.gribDataToFeatureCollection(it, "strom", "strom") } ?: "",
                    allGrids["rain"]?.let { GribOverlayUtil.gribDataToFeatureCollection(it, "rain", "rain") } ?: ""
                )

                mapLibreMap.getStyle { style ->
                    Log.d(TAG, "Adding GRIB overlay to map")
                    GribOverlayManager.addOrUpdateGribOverlay(context, style, geoJson)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        geoJson = geoJson
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading GRIB overlay", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error loading GRIB data"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
} 