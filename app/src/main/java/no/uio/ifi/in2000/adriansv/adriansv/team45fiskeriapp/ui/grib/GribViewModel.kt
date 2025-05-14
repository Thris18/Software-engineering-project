package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.grib.GribRepository
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib.utils.GribOverlayUtil
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib.GribData

private const val TAG = "GribViewModel"

data class GribUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val geoJson: String? = null,
    val gribData: Map<String, GribData?> = emptyMap()
)

@SuppressLint("StaticFieldLeak")
class GribViewModel(
    private val gribRepository: GribRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(GribUiState())
    val uiState: StateFlow<GribUiState> = _uiState.asStateFlow()

    init {
        initializeGribOverlay()
    }

    private fun initializeGribOverlay() {
        viewModelScope.launch {
            try {
                GribOverlayUtil.initialize(context)
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing GRIB overlay", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Unknown error initializing GRIB overlay"
                )
            }
        }
    }

    fun loadGribOverlayData() {
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
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    geoJson = geoJson
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error loading GRIB overlay", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error loading GRIB data"
                )
            }
        }
    }
} 