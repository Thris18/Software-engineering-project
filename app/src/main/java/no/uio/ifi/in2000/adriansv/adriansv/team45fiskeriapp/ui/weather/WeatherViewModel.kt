package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.weather

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.weather.WeatherRepository
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather.LocationWeather
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather.WeatherZoomLevel

private const val TAG = "WeatherViewModel"

data class WeatherUiState(
    val weather: LocationWeather? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()
    
    fun updateWeather(latitude: Double, longitude: Double, zoomLevel: Double) {
        Log.d(TAG, "Requesting weather update for lat=$latitude, lon=$longitude, zoom=$zoomLevel")
        
        val weatherZoomLevel = WeatherZoomLevel.fromZoom(zoomLevel)
        if (weatherZoomLevel == null) {
            Log.d(TAG, "Zoom level $zoomLevel is too far out for weather data")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                Log.d(TAG, "Starting weather fetch for zoom level: $weatherZoomLevel")
                val result = repository.getWeatherForLocation(latitude, longitude, weatherZoomLevel)
                result.onSuccess { locationWeather ->
                    Log.d(TAG, "Successfully fetched weather: temp=${locationWeather.temperature}°C, icon=${locationWeather.symbolCode}")
                    _uiState.update { it.copy(
                        weather = locationWeather,
                        isLoading = false,
                        error = null
                    )}
                }.onFailure { error ->
                    Log.e(TAG, "Failed to fetch weather", error)
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = error.message
                    )}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message
                )}
            }
        }
    }
}