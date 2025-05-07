package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.grib.GribRepository
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib.GribData
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib.GribPoint

data class GribUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedPoint: GribPoint? = null,
    val gribData: GribData? = null,
    val weatherGrids: Map<String, GribData?> = emptyMap(),
    val isOverlayVisible: Boolean = false,
    val overlayType: String? = null
)

class GribViewModel(
    private val gribRepository: GribRepository
) : ViewModel() {
    private val TAG = "GribViewModel"

    private val _uiState = MutableStateFlow(GribUiState())
    val uiState: StateFlow<GribUiState> = _uiState.asStateFlow()

    fun loadGribData(point: GribPoint) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                val gribData = gribRepository.getGribData(point)
                if (gribData != null) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            selectedPoint = point,
                            gribData = gribData
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Kunne ikke hente GRIB-data for valgt punkt"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Feil ved henting av GRIB-data: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "En feil oppstod ved henting av GRIB-data: ${e.message}"
                    )
                }
            }
        }
    }

    fun loadWeatherGrids() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                val grids = gribRepository.getAllWeatherGrids()
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        weatherGrids = grids
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Feil ved henting av værgrids: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "En feil oppstod ved henting av værgrids: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearSelection() {
        _uiState.update { 
            it.copy(
                selectedPoint = null,
                gribData = null
            )
        }
    }
}

class GribViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GribViewModel::class.java)) {
            val repository = GribRepository.GribRepositoryImpl(context)
            @Suppress("UNCHECKED_CAST")
            return GribViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 