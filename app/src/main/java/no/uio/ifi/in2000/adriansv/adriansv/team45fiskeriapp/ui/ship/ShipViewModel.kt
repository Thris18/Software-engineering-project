package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.ship

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.ship.ShipRepository.ShipRepositoryImpl
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.ship.Ship


private const val TAG = "ShipViewModel"
private const val UPDATE_INTERVAL = 15000L // 15 Seconds

data class ShipUIState(
    val ships: List<Ship> = emptyList(),
    val selectedShip: Ship? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ShipViewModel : ViewModel() {
    private val repository = ShipRepositoryImpl()
    private var updateJob: Job? = null

    private val _uiState = MutableStateFlow(ShipUIState(isLoading = true))
    val uiState: StateFlow<ShipUIState> = _uiState.asStateFlow()

    init {
        loadShips()
    }

    fun startPeriodicUpdates() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            while (true) {
                loadShips()
                delay(UPDATE_INTERVAL)
            }
        }
    }

    private fun loadShips() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.fetchShips().fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            ships = response.ships,
                            isLoading = false,
                            error = null
                        )
                    }
                },
                onFailure = { error ->
                    Log.e(TAG, "Error loading ships data", error)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Error loading ships: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        updateJob?.cancel()
    }
} 