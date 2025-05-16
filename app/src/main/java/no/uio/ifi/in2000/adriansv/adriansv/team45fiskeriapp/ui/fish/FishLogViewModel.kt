package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fish

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.fish.FishLog
import java.io.File
import java.io.FileOutputStream

data class FishLogUiState(
    val fishLogs: List<FishLog> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val failedImageLoads: Set<String> = emptySet(),
    val loadedImages: Set<String> = emptySet()
)

class FishLogViewModel(context: Context) : ViewModel() {
    private val repository = FishLogStorage(context)
    private val _uiState = MutableStateFlow(FishLogUiState())
    val uiState: StateFlow<FishLogUiState> = _uiState.asStateFlow()
    private val appContext = context.applicationContext

    init {
        viewModelScope.launch {
            repository.fishLogs.collect { logs ->
                _uiState.value = _uiState.value.copy(fishLogs = logs)
            }
        }
    }

    fun addFishLog(fishLog: FishLog) {
        if (fishLog.imageUri != null) {
            try {
                val sourceUri = fishLog.imageUri.toUri()
                val imageId = "fish_${fishLog.timestamp}"
                val destinationFile = File(appContext.filesDir, "$imageId.jpg")
                
                appContext.contentResolver.openInputStream(sourceUri)?.use { input ->
                    FileOutputStream(destinationFile).use { output ->
                        input.copyTo(output)
                    }
                }

                val updatedFishLog = fishLog.copy(
                    imageUri = Uri.fromFile(destinationFile).toString()
                )
                repository.addFishLog(updatedFishLog)
            } catch (e: Exception) {
                repository.addFishLog(fishLog)
            }
        } else {
            repository.addFishLog(fishLog)
        }
    }

    fun clearFishLogs() {
        _uiState.value.fishLogs.forEach { fishLog ->
            if (fishLog.imageUri != null) {
                try {
                    val imageId = "fish_${fishLog.timestamp}"
                    File(appContext.filesDir, imageId).delete()
                } catch (_: Exception) {
                }
            }
        }
        repository.clearFishLogs()
        _uiState.value = _uiState.value.copy(
            failedImageLoads = emptySet(),
            loadedImages = emptySet()
        )
    }

    fun addFailedImageLoad(imageId: String) {
        _uiState.value = _uiState.value.copy(
            failedImageLoads = _uiState.value.failedImageLoads + imageId
        )
    }

    fun addLoadedImage(imageId: String) {
        _uiState.value = _uiState.value.copy(
            loadedImages = _uiState.value.loadedImages + imageId
        )
    }

    fun removeFishLog(fishLog: FishLog) {
        viewModelScope.launch {
            repository.removeFishLog(fishLog)

            _uiState.value = _uiState.value.copy(
                fishLogs = _uiState.value.fishLogs.filter { it.timestamp != fishLog.timestamp }
            )
        }
    }
} 