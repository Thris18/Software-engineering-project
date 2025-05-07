package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.grib

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib.GribData
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib.GribPoint
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib.GribDirectionUtil

/**
 * Grensesnitt for tilgang til GRIB (Gridded Binary) værvarslingsdata.
 * GRIB er et format som ofte brukes i meteorologi for å lagre historiske og prognostiserte værvarslingsdata.
 */
interface GribRepository {
    /**
     * Henter GRIB-data for et spesifikt geografisk punkt.
     * @param point Det geografiske punktet å hente værvarslingsdata for
     * @return GribData-objekt som inneholder værvarslingsinformasjon, eller null hvis data ikke kunne hentes
     */
    suspend fun getGribData(point: GribPoint): GribData?

    /**
     * Henter et spesifikt GRIB-datagrid for en gitt type og variabel.
     * @param type Typen GRIB-data som skal hentes (f.eks. "weather", "current", "waves")
     * @param variableName Den spesifikke variabelen som skal hentes (f.eks. "u-component_of_wind_height_above_ground")
     * @return GribData-objekt som inneholder griddata, eller null hvis data ikke kunne hentes
     */
    suspend fun getGribDataGrid(type: String, variableName: String?): GribData?

    /**
     * Henter alle værvarslingsrelaterte GRIB-datagrids.
     * @return Map som inneholder ulike typer værvarslingsdata (vind, bølger, strøm, regn)
     */
    suspend fun getAllWeatherGrids(): Map<String, GribData?>

    /**
     * Implementasjon av GribRepository-grensesnittet.
     * 
     * @property context Android Context som kreves for filoperasjoner og dataaksess.
     *                  Dette brukes av GribDataSource for å laste ned og lagre GRIB-filer.
     * @property gribDataSource Datakilden for GRIB-filer. Standardimplementasjon bruker den oppgitte context.
     * @property gribParser Parser for GRIB-filer. Standardimplementasjon er tilgjengelig.
     * @property gribDataProcessor Prosessor for GRIB-data. Standardimplementasjon er tilgjengelig.
     * 
     * Brukseksempler:
     * ```kotlin
     * // Grunnleggende bruk med kun context
     * val repository = GribRepository.GribRepositoryImpl(context)
     * 
     * // Tilpasset datakilde
     * val customDataSource = GribDataSource(context)
     * val repository = GribRepository.GribRepositoryImpl(context, customDataSource)
     * 
     * // Fullstendig tilpasset implementasjon
     * val repository = GribRepository.GribRepositoryImpl(
     *     context = context,
     *     gribDataSource = customDataSource,
     *     gribParser = customParser,
     *     gribDataProcessor = customProcessor
     * )
     * ```
     */
    class GribRepositoryImpl(
        private val context: Context,
        private val gribDataSource: GribDataSource = GribDataSource(context),
        private val gribParser: GribParser = GribParser(),
        private val gribDataProcessor: GribDataProcessor = GribDataProcessor()
    ) : GribRepository {
        private val TAG = "GribRepositoryImpl"

        override suspend fun getGribData(point: GribPoint): GribData? = withContext(Dispatchers.IO) {
            try {
                // Hent værvarsel data
                val weatherFile = gribDataSource.downloadGribFile("weather")
                if (weatherFile == null) {
                    Log.e(TAG, "Kunne ikke laste ned weather GRIB-fil")
                    return@withContext null
                }

                // Hent strøm data
                val currentFile = gribDataSource.downloadGribFile("current")
                if (currentFile == null) {
                    Log.e(TAG, "Kunne ikke laste ned current GRIB-fil")
                    return@withContext null
                }

                // Hent bølge data
                val wavesFile = gribDataSource.downloadGribFile("waves")
                if (wavesFile == null) {
                    Log.e(TAG, "Kunne ikke laste ned waves GRIB-fil")
                    return@withContext null
                }

                // Prosesser dataene
                gribDataProcessor.processGribData(point, weatherFile, currentFile, wavesFile)
            } catch (e: Exception) {
                Log.e(TAG, "Feil ved henting av GRIB-data: ${e.message}", e)
                null
            }
        }

        // Hjelpefunksjon: Hent hele gridet for én type
        override suspend fun getGribDataGrid(type: String, variableName: String?): GribData? = withContext(Dispatchers.IO) {
            val file = gribDataSource.downloadGribFile(type)
            return@withContext if (file != null) GribParser.parseGribFile(file, variableName) else null
        }

        // Hent alle relevante grids for overlay
        override suspend fun getAllWeatherGrids(): Map<String, GribData?> = withContext(Dispatchers.IO) {
            // Hent vind-data med både u- og v-komponenter
            val windU = getGribDataGrid("weather", "u-component_of_wind_height_above_ground")
            val windV = getGribDataGrid("weather", "v-component_of_wind_height_above_ground")
            val wind = if (windU != null && windV != null) {
                windU.copy(
                    uValues = windU.values,
                    vValues = windV.values,
                    windSpeed = GribDirectionUtil.calculateSpeedFromUV(windU.values[0].toDouble(), windV.values[0].toDouble()),
                    windDirection = GribDirectionUtil.calculateDirectionFromUV(windU.values[0].toDouble(), windV.values[0].toDouble())
                )
            } else null

            // Hent strøm-data med både u- og v-komponenter
            val currentU = getGribDataGrid("current", "u-component_of_current_depth_below_sea")
            val currentV = getGribDataGrid("current", "v-component_of_current_depth_below_sea")
            val strom = if (currentU != null && currentV != null) {
                currentU.copy(
                    uValues = currentU.values,
                    vValues = currentV.values,
                    currentSpeed = GribDirectionUtil.calculateSpeedFromUV(currentU.values[0].toDouble(), currentV.values[0].toDouble()),
                    currentDirection = GribDirectionUtil.calculateDirectionFromUV(currentU.values[0].toDouble(), currentV.values[0].toDouble())
                )
            } else null

            val wave = getGribDataGrid("waves", "Significant_height_of_combined_wind_waves_and_swell_height_above_ground")
            val rain = getGribDataGrid("weather", "Total_precipitation_height_above_ground")
            
            mapOf(
                "wind" to wind,
                "wave" to wave,
                "strom" to strom,
                "rain" to rain
            )
        }
    }
}