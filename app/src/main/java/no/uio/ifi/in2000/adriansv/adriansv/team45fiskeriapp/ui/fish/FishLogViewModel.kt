package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fish

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.fish.FishLogRepository
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.fish.FishLog

data class FishLogUiState(
    val fishLogs: List<FishLog> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FishLogViewModel(context: Context) : ViewModel() {
    private val repository = FishLogRepository(context)
    private val _uiState = MutableStateFlow(FishLogUiState())
    val uiState: StateFlow<FishLogUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.fishLogs.collect { logs ->
                _uiState.value = _uiState.value.copy(fishLogs = logs)
            }
        }
    }

    fun addFishLog(fishLog: FishLog) {
        repository.addFishLog(fishLog)
    }

    fun clearFishLogs() {
        repository.clearFishLogs()
    }
} 