package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.farevarsel.GeoJsonDataSource
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.farevarsel.GeoJsonRepository

class GeoJsonViewModel : ViewModel() {
    // Create repository directly in the ViewModel
    private val repository = GeoJsonRepository.GeoJsonRepositoryImpl(GeoJsonDataSource())

    // Just a single StateFlow for the GeoJSON data
    private val _geoJsonData = MutableStateFlow<String?>(null)
    val geoJsonData: StateFlow<String?> = _geoJsonData.asStateFlow()

    init {
        loadGeoJsonData()
    }

    private fun loadGeoJsonData() {
        viewModelScope.launch {
            repository.fetchGeoJson().fold(
                onSuccess = { data -> _geoJsonData.value = data },
                onFailure = { println("Error loading GeoJSON: ${it.message}") }
            )
        }
    }
}