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
import java.net.URLEncoder
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private const val TAG = "GeoJsonViewModel"
private const val MAPTILER_API_KEY = "oMZQoq4zniKOHeMvi7oA"

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
                val encodedQuery = URLEncoder.encode(query, "UTF-8")
                val url = "https://api.maptiler.com/geocoding/$encodedQuery.json?key=$MAPTILER_API_KEY&language=no&limit=1"
                val response = URL(url).readText()
                val jsonResponse = JSONObject(response)
                val features = jsonResponse.getJSONArray("features")
                
                if (features.length() > 0) {
                    val feature = features.getJSONObject(0)
                    val geometry = feature.getJSONObject("geometry")
                    val coordinates = geometry.getJSONArray("coordinates")
                    LatLng(coordinates.getDouble(1), coordinates.getDouble(0))
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Geocoding error: ${e.message}", e)
                null
            }
        }
    }

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

                // Hent brukerens posisjon fra SharedPreferences
                val prefs = applicationContext.getSharedPreferences("user_location", Context.MODE_PRIVATE)
                val userLat = prefs.getFloat("latitude", 59.9139f)
                val userLon = prefs.getFloat("longitude", 10.7522f)

                for (i in 0 until features.length()) {
                    val feature = features.getJSONObject(i)
                    val properties = feature.getJSONObject("properties")
                    val geometry = feature.getJSONObject("geometry")
                    val coordinates = geometry.getJSONArray("coordinates")
                    val lon = coordinates.getDouble(0)
                    val lat = coordinates.getDouble(1)

                    // Beregn avstand fra brukerens posisjon
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
                Log.e("GeoJsonViewModel", "Error fetching search suggestions: ${e.message}")
                _uiState.update { it.copy(searchSuggestions = emptyList()) }
            }
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Jordens radius i kilometer

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