package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.alerts

import android.annotation.SuppressLint
import android.content.Context
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
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.alerts.GeoJsonRepository
import org.json.JSONObject
import org.maplibre.android.geometry.LatLng
import java.net.URL
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private const val TAG = "GeoJsonViewModel"

// UI State for MapScreen
data class MapUiState(
    val geoJsonData: String? = null,
    val selectedAlert: JSONObject? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchSuggestions: List<SearchSuggestion> = emptyList()
)

data class SearchSuggestion(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val distance: String? = null
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
                            error = error.message
                        )
                    }
                }
            )
        }
    }

    // Search functionality
    private val _searchTarget = MutableStateFlow<LatLng?>(null)
    val searchTarget: StateFlow<LatLng?> = _searchTarget

    fun getSearchSuggestions(query: String) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val url = "https://api.maptiler.com/geocoding/$query.json?key=oMZQoq4zniKOHeMvi7oA&language=no&limit=10&country=no&types=place,locality,neighbourhood,address,postal_code"
                    URL(url).openStream().bufferedReader().use { it.readText() }
                }

                val jsonObject = JSONObject(response)
                val features = jsonObject.getJSONArray("features")
                val suggestions = mutableListOf<SearchSuggestion>()

                // Get user location
                val prefs = applicationContext.getSharedPreferences("user_location", Context.MODE_PRIVATE)
                val userLat = prefs.getFloat("latitude", 59.9139f)
                val userLon = prefs.getFloat("longitude", 10.7522f)

                for (i in 0 until features.length()) {
                    val feature = features.getJSONObject(i)
                    feature.getJSONObject("properties")
                    val geometry = feature.getJSONObject("geometry")
                    val coordinates = geometry.getJSONArray("coordinates")
                    val lon = coordinates.getDouble(0)
                    val lat = coordinates.getDouble(1)

                    // Calculate distance from user location
                    val distance = calculateDistance(userLat.toDouble(), userLon.toDouble(), lat, lon)
                    val formattedDistance = formatDistance(distance)

                    suggestions.add(
                        SearchSuggestion(
                            name = feature.getString("place_name"),
                            latitude = lat,
                            longitude = lon,
                            distance = formattedDistance
                        )
                    )
                }

                _uiState.update { it.copy(searchSuggestions = suggestions) }
            } catch (e: Exception) {
                _uiState.update { it.copy(searchSuggestions = emptyList()) }
            }
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earths radius in kilometers

        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(lonDistance / 2) * sin(lonDistance / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return r * c
    }

    @SuppressLint("DefaultLocale")
    private fun formatDistance(distance: Double): String {
        return when {
            distance < 1 -> "${(distance * 1000).toInt()} m"
            else -> String.format("%.1f km", distance)
        }
    }

    fun selectSuggestion(suggestion: SearchSuggestion) {
        _searchTarget.value = LatLng(suggestion.latitude, suggestion.longitude)
        _uiState.update { it.copy(searchSuggestions = emptyList()) }
    }

    fun setSelectedAlert(alert: JSONObject?) {
        _uiState.update { it.copy(selectedAlert = alert) }
    }


}