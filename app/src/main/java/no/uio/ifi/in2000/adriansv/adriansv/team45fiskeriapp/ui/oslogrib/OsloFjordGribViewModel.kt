package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.oslogrib


import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.grib.GribDataSource
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib.GribData
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib.GribPoint
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.grib.GribRepository


// UI-tilstand/Dataklasse for visualisering av GRIB-data
data class OslofjordGribUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val gribData: GribData? = null,
    val nedbørData: GribData? = null,    // Precipitation/Nedbør
    val vindData: GribData? = null,      // Wind/Vind
    val strømData: GribData? = null,     // Current/Strøm
    val bølgehøydeData: GribData? = null, // Wave height/Bølgehøyde
    val selectedContentType: String = "nedbør", // Default to precipitation
    val selectedPoint: GribPoint? = null,
    val allPoints: Map<String, GribPoint?> = emptyMap(), // Lagrer alle datapunkter
    val gribLayerVisible: Boolean = false,
    val gribImagePath: String? = null,
    val gribFilePath: String? = null
)


class OslofjordGribViewModel(
    val repository: GribRepository = GribDataSource()
) : ViewModel() {
    private val TAG = "OslofjordGribViewModel"

    private val _uiState = MutableStateFlow(OslofjordGribUiState())
    val uiState: StateFlow<OslofjordGribUiState> = _uiState.asStateFlow()

    private val contentTypeMapping = mapOf(
        "nedbør" to "weather", // Bruker weather type for nedbør
        "vind" to "weather",   // Bruker weather type for vind
        "strøm" to "current",  // Strøm
        "bølgehøyde" to "waves" // Bølgehøyde
    )


    val norwegianContentTypes = listOf("nedbør", "strøm", "vind", "bølgehøyde")

    // Laster alle datatyper automatisk når viewmodellen blir lagd when viewmodel is created
    init {
        viewModelScope.launch {
            // Wait until we have a context before loading data
            // This will be set when loadAllGribData is first called
        }
    }

    // Laster alle GRIB-datatyper for Oslofjord-området
    fun loadAllGribData(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(
                isLoading = true,
                error = null
            ) }

            try {
                Log.d(TAG, "Loading all Oslofjord GRIB data types")

                // Laster alle content-typer
                norwegianContentTypes.forEach { norwegianType ->
                    loadDataType(context, norwegianType)
                }

                // Oppdaterer UI-tilstand etter å ha lastet inn all data
                _uiState.update { it.copy(
                    isLoading = false,
                    gribLayerVisible = true
                ) }

                Log.d(TAG, "Successfully loaded all Oslofjord GRIB data types")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading all Oslofjord GRIB data: ${e.message}", e)
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Error: ${e.message}"
                ) }
            }
        }
    }

    // Hjelpemetode for å laste en spesifikk datatype
    private suspend fun loadDataType(context: Context, norwegianType: String) {
        try {
            // Henter API-content type basert på Norwegian content type
            val apiContentType = contentTypeMapping[norwegianType] ?: return

            Log.d(TAG, "Loading Oslofjord GRIB data for type: $norwegianType (API: $apiContentType)")

            // Laster ned GRIB-filen
            val gribFile = repository.downloadOslofjordGribFile(context, apiContentType)

            if (gribFile == null) {
                _uiState.update { it.copy(
                    error = "Failed to download GRIB file for $norwegianType"
                ) }
                return
            }

            // Parser GRIB-filen
            val gribData = repository.loadGribData(gribFile)

            if (gribData == null) {
                _uiState.update { it.copy(
                    error = "Failed to parse GRIB file for $norwegianType: ${gribFile.absolutePath}"
                ) }
                return
            }


            // For vær-content, så må vi enten velge nedbør eller vind basert på variabelnavn i GRIB-dataen
            var dataToStore = gribData

            // Hvis dette er vær-data filen, prøv å hent riktig variabel
            if (apiContentType == "weather") {
                when (norwegianType) {
                    "nedbør" -> {
                        // Se etter nedbør-relaterte variabler
                        if (!gribData.variableName.contains("Precipitation", ignoreCase = true) &&
                            !gribData.variableName.contains("rain", ignoreCase = true)) {
                            // Hvis vi ikke finner nedbørdata (precipitation data), bruk hva som er tilgjengelig
                            // Men gi den nytt navn for UI
                            dataToStore = gribData.copy(variableName = "Nedbør")
                        }
                    }
                    "vind" -> {
                        // Se etter vind-relaterte variabler
                        if (!gribData.variableName.contains("Wind", ignoreCase = true) &&
                            !gribData.variableName.contains("speed", ignoreCase = true)) {
                            // Hvis vi ikke finner vind-data, bruk det som er tilgjenglig
                            // Men gi den nytt navn for UI-en
                            dataToStore = gribData.copy(variableName = "Vind")
                        }
                    }
                }
            } else if (apiContentType == "waves") {
                // Gi nytt navn til bølgehøyde variabelen
                dataToStore = gribData.copy(variableName = "Bølgehøyde")
            } else if (apiContentType == "current") {
                // Gi nytt navn til strøm variabelen
                dataToStore = gribData.copy(variableName = "Strøm")
            }

            // Konverter til bilde
            val imagePath = repository.convertGribToImage(dataToStore, context)

            // Oppdater UI-tilstand basert på datatype
            when (norwegianType) {
                "nedbør" -> {
                    _uiState.update { it.copy(
                        nedbørData = dataToStore,
                        gribData = dataToStore,  // Sett til nåværende aktiv data
                        gribFilePath = gribFile.absolutePath,
                        gribImagePath = imagePath,
                        selectedContentType = "nedbør"
                    ) }
                }
                "vind" -> {
                    _uiState.update { it.copy(
                        vindData = dataToStore
                    ) }
                }
                "strøm" -> {
                    _uiState.update { it.copy(
                        strømData = dataToStore
                    ) }
                }
                "bølgehøyde" -> {
                    _uiState.update { it.copy(
                        bølgehøydeData = dataToStore
                    ) }
                }
            }

            Log.d(TAG, "Successfully loaded $norwegianType GRIB data: ${dataToStore.variableName}, min: ${dataToStore.minValue}, max: ${dataToStore.maxValue}")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading $norwegianType GRIB data: ${e.message}", e)
            _uiState.update { it.copy(
                error = "Error loading $norwegianType: ${e.message}"
            ) }
        }
    }


    // Last spesifikk content type og gjør den aktiv
    fun loadOslofjordGribData(context: Context, norwegianType: String = "nedbør") {
        viewModelScope.launch {
            _uiState.update { it.copy(
                isLoading = true,
                error = null,
                selectedContentType = norwegianType
            ) }

            // Se om vi allerede har denne datatypen lastet inn
            val existingData = when (norwegianType) {
                "nedbør" -> _uiState.value.nedbørData
                "vind" -> _uiState.value.vindData
                "strøm" -> _uiState.value.strømData
                "bølgehøyde" -> _uiState.value.bølgehøydeData
                else -> null
            }

            if (existingData != null) {
                // Om vi allerede har denne dataen, gjør den aktiv
                _uiState.update { it.copy(
                    isLoading = false,
                    gribData = existingData,
                    gribLayerVisible = true
                ) }
                return@launch
            }

            // Vi har ikke denne datatypen enda, last den
            try {
                loadDataType(context, norwegianType)

                _uiState.update { it.copy(
                    isLoading = false,
                    gribLayerVisible = true
                ) }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Oslofjord GRIB data: ${e.message}", e)
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Error: ${e.message}"
                ) }
            }
        }
    }


    // Hent GRIB-data på spesifikke koordinater
    fun getGribDataAtCoordinates(latitude: Double, longitude: Double) {
        val dataTypes = mapOf(
            "nedbør" to _uiState.value.nedbørData,
            "strøm" to _uiState.value.strømData,
            "vind" to _uiState.value.vindData,
            "bølgehøyde" to _uiState.value.bølgehøydeData
        )

        // Initialiser punkt-kolleksjon
        val points = mutableMapOf<String, GribPoint?>()

        // Hent data for hver tilgjengelige type
        dataTypes.forEach { (type, data) ->
            if (data != null) {
                try {
                    val value = repository.getValueAtCoordinates(data, latitude, longitude)
                    if (value != null) {
                        points[type] = GribPoint(
                            latitude = latitude,
                            longitude = longitude,
                            value = value,
                            variableName = data.variableName,
                            unit = data.unit
                        )
                        Log.d(TAG, "$type value at ($latitude, $longitude): $value ${data.unit}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting $type data: ${e.message}", e)
                }
            }
        }

        if (points.isNotEmpty()) {
            // Sett valgt punkt til aktiv content type
            val activePoint = points[_uiState.value.selectedContentType] ?: points.values.firstOrNull()

            // Lagre alle punkter og valgt punkt i UI-tilstanden
            _uiState.update { it.copy(
                selectedPoint = activePoint,
                // Lagre alle punkter i et felt for å vise
                allPoints = points
            ) }
        } else {
            _uiState.update { it.copy(
                error = "No GRIB data available at the selected coordinates"
            ) }
        }
    }


    // Bytt mellom forskjellige innlastede datatyper
    fun switchDataType(norwegianType: String) {
        val dataToShow = when (norwegianType) {
            "nedbør" -> _uiState.value.nedbørData
            "vind" -> _uiState.value.vindData
            "strøm" -> _uiState.value.strømData
            "bølgehøyde" -> _uiState.value.bølgehøydeData
            else -> null
        }

        if (dataToShow != null) {
            _uiState.update { it.copy(
                gribData = dataToShow,
                selectedContentType = norwegianType,
                selectedPoint = null // Clear any selected point when switching data
            ) }
        } else {
            _uiState.update { it.copy(
                error = "Data for $norwegianType is not loaded yet"
            ) }
        }
    }

//    fun toggleGribLayerVisibility() {
//        _uiState.update { it.copy(
//            gribLayerVisible = !it.gribLayerVisible
//        ) }
//    }
//
//    fun clearError() {
//        _uiState.update { it.copy(error = null) }
//    }
}