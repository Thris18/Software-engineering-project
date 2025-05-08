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
import java.net.URL
import java.net.URLEncoder
import org.json.JSONArray
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

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
    val longitude: Double
)

class GeoJsonViewModel(private val applicationContext: Context) : ViewModel() {
    private val repository = GeoJsonRepository.GeoJsonRepositoryImpl()
    private val httpClient = OkHttpClient()

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
            if (query.isEmpty()) {
                _uiState.update { it.copy(searchSuggestions = emptyList()) }
                return@launch
            }

            try {
                withContext(Dispatchers.IO) {
                    val encodedQuery = URLEncoder.encode(query, "UTF-8")
                    val url = "https://api.maptiler.com/geocoding/$encodedQuery.json?key=$MAPTILER_API_KEY&language=no&limit=10&country=no&types=place,locality,neighbourhood,address,postal_code"
                    Log.d(TAG, "Fetching suggestions from URL: $url")
                    
                    val request = Request.Builder()
                        .url(url)
                        .addHeader("Accept", "application/json")
                        .build()

                    val response = httpClient.newCall(request).execute()
                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string()
                        Log.e(TAG, "API Error Response: $errorBody")
                        throw IOException("Unexpected response code: ${response.code}, Error: $errorBody")
                    }

                    val responseBody = response.body?.string()
                    if (responseBody == null) {
                        throw IOException("Empty response body")
                    }
                    
                    Log.d(TAG, "Received response: $responseBody")
                    
                    val jsonResponse = JSONObject(responseBody)
                    val features = jsonResponse.getJSONArray("features")
                    Log.d(TAG, "Number of features found: ${features.length()}")
                    
                    val suggestions = mutableListOf<SearchSuggestion>()
                    for (i in 0 until features.length()) {
                        val feature = features.getJSONObject(i)
                        val properties = feature.getJSONObject("properties")
                        val geometry = feature.getJSONObject("geometry")
                        val coordinates = geometry.getJSONArray("coordinates")
                        
                        val suggestion = SearchSuggestion(
                            name = feature.getString("place_name"),
                            latitude = coordinates.getDouble(1),
                            longitude = coordinates.getDouble(0)
                        )
                        Log.d(TAG, "Adding suggestion: ${suggestion.name}")
                        suggestions.add(suggestion)
                    }
                    
                    Log.d(TAG, "Total suggestions: ${suggestions.size}")
                    _uiState.update { it.copy(searchSuggestions = suggestions) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting search suggestions: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        searchSuggestions = emptyList(),
                        error = "Kunne ikke hente søkeforslag: ${e.message}"
                    ) 
                }
            }
        }
    }

    fun selectSuggestion(suggestion: SearchSuggestion) {
        _searchTarget.value = LatLng(suggestion.latitude, suggestion.longitude)
        _uiState.update { it.copy(searchSuggestions = emptyList()) }
    }

    fun setSelectedAlert(alert: JSONObject?) {
        _uiState.update { it.copy(selectedAlert = alert) }
    }

    fun retry() {
        loadGeoJsonData()
    }
}