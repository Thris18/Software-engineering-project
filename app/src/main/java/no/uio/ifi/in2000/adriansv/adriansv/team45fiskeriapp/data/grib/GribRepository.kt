package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.grib

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib.GribData
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib.GribPoint
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.core.utils.CalculateUtil
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * GRIB-DATA REPOSITORIUM FOR FISKERIAPPLIKASJONEN
 * 
 * Denne filen implementerer repositoriet som håndterer nedlasting, behandling og 
 * fremskaffelse av meteorologiske data i GRIB-format (Gridded Binary), som er
 * et standardformat for værdataformidling.
 * 
 * Nøkkelfunksjonalitet:
 * - Nedlasting av GRIB-filer for vær, havstrømmer og bølger
 * - Parsing av binære GRIB-filer til strukturerte datamodeller
 * - Ekstrahering av spesifikke værparametere fra GRIB-data
 * - Beregning av vindstyrke og -retning fra u- og v-komponenter
 * - Beregning av strømstyrke og -retning fra u- og v-komponenter
 * - Interpolering av verdier for spesifikke geografiske koordinater
 * - Håndtering av manglende eller ugyldige datapunkter
 * 
 * Filen inneholder:
 * - GribRepository: Interface som definerer grensesnittet for GRIB-data tilgang
 * - GribRepositoryImpl: Implementasjon av interfacet med alle detaljene
 * - Hjelpefunksjoner for å finne og interpolere verdier på spesifikke koordinater
 * 
 * Denne komponenten er grunnleggende for appens evne til å vise detaljerte værdata
 * direkte på kartet, noe som gjør det mulig for fiskere å vurdere værforholdene
 * og planlegge sine fisketurer mer effektivt og sikkert.
 */

private const val TAG = "GribRepository"

interface GribRepository {
    suspend fun getGribData(point: GribPoint): GribData?
    suspend fun getGribDataGrid(type: String, variableName: String? = null): GribData?
    suspend fun getAllWeatherGrids(): Map<String, GribData?>

    class GribRepositoryImpl(context: Context) : GribRepository {
        private val gribDataSource = GribDataSource(context)

        override suspend fun getGribData(point: GribPoint): GribData? = withContext(Dispatchers.IO) {
            // Fetch weather data
            try {
                val weatherFile = gribDataSource.downloadGribFile("weather") ?: return@withContext null
                val weatherData = GribParser.parseGribFile(weatherFile) ?: return@withContext null

                // Fetch pressure data
                val pressureData = GribParser.parseGribFile(weatherFile, "Pressure_height_above_ground")
                    ?: return@withContext null
                val pressure = getValueAtCoordinates(pressureData, point.latitude, point.longitude)

                // Fetch wind data
                val uWindData = GribParser.parseGribFile(
                    weatherFile,
                    "u-component_of_wind_height_above_ground"
                ) ?: return@withContext null
                val uWind = getValueAtCoordinates(uWindData, point.latitude, point.longitude)

                val vWindData = GribParser.parseGribFile(
                    weatherFile,
                    "v-component_of_wind_height_above_ground"
                ) ?: return@withContext null
                val vWind = getValueAtCoordinates(vWindData, point.latitude, point.longitude)

                // Calculate windspeed and direction
                val windSpeed = if (uWind != null && vWind != null) {
                    sqrt(uWind * uWind + vWind * vWind)
                } else null

                val windDirection = if (uWind != null && vWind != null) {
                    // Calculate direction in degrees (0-360)
                    val direction = Math.toDegrees(atan2(vWind.toDouble(), uWind.toDouble()))
                    // Convert to meteorological direction (where wind is blowing)
                    ((direction + 180) % 360).toFloat()
                } else null

                // Get precipitation data
                val precipitationData = GribParser.parseGribFile(
                    weatherFile,
                    "Total_precipitation_height_above_ground"
                ) ?: return@withContext null
                val precipitation = getValueAtCoordinates(precipitationData, point.latitude, point.longitude)

                // Get height data
                val heightData = GribParser.parseGribFile(weatherFile, "height_above_ground") ?: return@withContext null
                val height = getValueAtCoordinates(heightData, point.latitude, point.longitude)

                val height1Data = GribParser.parseGribFile(weatherFile, "height_above_ground1") ?: return@withContext null
                val height1 = getValueAtCoordinates(height1Data, point.latitude, point.longitude)

                // Get time data
                val timeData = GribParser.parseGribFile(weatherFile, "time") ?: return@withContext null
                val time = getValueAtCoordinates(timeData, point.latitude, point.longitude)?.toString() ?: "0"

                val reftimeData = GribParser.parseGribFile(weatherFile, "reftime") ?: return@withContext null
                val reftime = getValueAtCoordinates(reftimeData, point.latitude, point.longitude)?.toString() ?: "0"

                // Get current data
                val currentFile = gribDataSource.downloadGribFile("current")
                if (currentFile == null) {
                    Log.e(TAG, "Kunne ikke laste ned current GRIB-fil")
                    return@withContext null
                }

                // Get u- and v-component of current
                val uCurrentData = GribParser.parseGribFile(
                    currentFile,
                    "u-component_of_current_depth_below_sea"
                ) ?: return@withContext null
                val uCurrent = getValueAtCoordinates(uCurrentData, point.latitude, point.longitude)

                val vCurrentData = GribParser.parseGribFile(
                    currentFile,
                    "v-component_of_current_depth_below_sea"
                ) ?: return@withContext null
                val vCurrent = getValueAtCoordinates(vCurrentData, point.latitude, point.longitude)

                // Calculate current speed and direction
                val currentSpeed = if (uCurrent != null && vCurrent != null) {
                    sqrt(uCurrent * uCurrent + vCurrent * vCurrent)
                } else null

                val currentDirection = if (uCurrent != null && vCurrent != null) {
                    // Calculate direction in degrees (0-360)
                    val direction = Math.toDegrees(atan2(vCurrent.toDouble(), uCurrent.toDouble()))
                    // Convert to meteorological direction (where wind is blowing)
                    ((direction + 180) % 360).toFloat()
                } else null

                // Get waves data
                val wavesFile = gribDataSource.downloadGribFile("waves") ?: return@withContext null
                val wavesData = GribParser.parseGribFile(wavesFile) ?: return@withContext null
                val waveHeight = getValueAtCoordinates(wavesData, point.latitude, point.longitude)
                val waveDirection = getValueAtCoordinates(wavesData, point.latitude, point.longitude)

                // Create GribData object
                return@withContext GribData(
                    values = weatherData.values,
                    width = weatherData.width,
                    height = weatherData.height,
                    latitudes = weatherData.latitudes,
                    longitudes = weatherData.longitudes,
                    minValue = weatherData.minValue,
                    maxValue = weatherData.maxValue,
                    variableName = "weather",
                    unit = "mixed",
                    referenceTime = weatherData.referenceTime,
                    temperature = null,
                    windSpeed = windSpeed,
                    windDirection = windDirection,
                    waveHeight = waveHeight,
                    waveDirection = waveDirection,
                    currentSpeed = currentSpeed,
                    currentDirection = currentDirection,
                    pressure = pressure,
                    precipitation = precipitation,
                    lat = point.latitude.toFloat(),
                    lon = point.longitude.toFloat(),
                    time = time,
                    reftime = reftime,
                    heightAboveGround = height ?: 0f,
                    heightAboveGround1 = height1 ?: 0f
                )
            } catch (e: Exception) {
                null
            }
        }

        private fun getValueAtCoordinates(gribData: GribData, latitude: Double, longitude: Double): Float? {
            try {

                var closestLatIndex = -1
                var closestLonIndex = -1
                var minLatDiff = Double.MAX_VALUE
                var minLonDiff = Double.MAX_VALUE

                val minLat = gribData.latitudes.minOrNull() ?: 0f
                val maxLat = gribData.latitudes.maxOrNull() ?: 0f
                val minLon = gribData.longitudes.minOrNull() ?: 0f
                val maxLon = gribData.longitudes.maxOrNull() ?: 0f

                if (latitude < minLat || latitude > maxLat || longitude < minLon || longitude > maxLon) {
                    return null
                }

                for (i in gribData.latitudes.indices) {
                    val diff = abs(gribData.latitudes[i] - latitude)
                    if (diff < minLatDiff) {
                        minLatDiff = diff
                        closestLatIndex = i
                    }
                }

                for (i in gribData.longitudes.indices) {
                    val diff = abs(gribData.longitudes[i] - longitude)
                    if (diff < minLonDiff) {
                        minLonDiff = diff
                        closestLonIndex = i
                    }
                }

                if (closestLatIndex >= 0 && closestLonIndex >= 0) {
                    if (minLatDiff > 0.5 || minLonDiff > 0.5) {
                        return (gribData.minValue + gribData.maxValue) / 2
                    }

                    val index = closestLatIndex * gribData.width + closestLonIndex

                    if (index < gribData.values.size) {
                        val value = gribData.values[index]

                        if (value.isNaN()) {

                            for (radius in 1..3) {
                                val validNeighbor = findValidNeighborValue(gribData, closestLatIndex, closestLonIndex, radius)
                                if (validNeighbor != null) {
                                    return validNeighbor
                                }
                            }

                            return (gribData.minValue + gribData.maxValue) / 2
                        }

                        return value
                    }
                }
                return null
            } catch (e: Exception) {
                return null
            }
        }

        private fun findValidNeighborValue(gribData: GribData, centerLatIdx: Int, centerLonIdx: Int, radius: Int): Float? {
            val width = gribData.width
            val height = gribData.height
            val values = gribData.values

            for (latOffset in -radius..radius) {
                for (lonOffset in -radius..radius) {
                    if (latOffset == 0 && lonOffset == 0) continue

                    val latIdx = centerLatIdx + latOffset
                    val lonIdx = centerLonIdx + lonOffset

                    if (latIdx in 0..<height && lonIdx >= 0 && lonIdx < width) {
                        val index = latIdx * width + lonIdx
                        if (index >= 0 && index < values.size) {
                            val value = values[index]
                            if (!value.isNaN()) {
                                return value
                            }
                        }
                    }
                }
            }
            return null
        }

        override suspend fun getGribDataGrid(type: String, variableName: String?): GribData? = withContext(Dispatchers.IO) {
            val file = gribDataSource.downloadGribFile(type)
            return@withContext if (file != null) GribParser.parseGribFile(file, variableName) else null
        }
        
        override suspend fun getAllWeatherGrids(): Map<String, GribData?> = withContext(Dispatchers.IO) {
            // Get vind-data med både u- og v-komponenter
            val windU = getGribDataGrid("weather", "u-component_of_wind_height_above_ground")
            val windV = getGribDataGrid("weather", "v-component_of_wind_height_above_ground")
            val wind = if (windU != null && windV != null) {
                windU.copy(
                    uValues = windU.values,
                    vValues = windV.values,
                    windSpeed = CalculateUtil.calculateSpeedFromUV(windU.values[0].toDouble(), windV.values[0].toDouble()),
                    windDirection = CalculateUtil.calculateDirectionFromUV(windU.values[0].toDouble(), windV.values[0].toDouble())
                )
            } else null

            // Get current-data med både u- og v-komponenter
            val currentU = getGribDataGrid("current", "u-component_of_current_depth_below_sea")
            val currentV = getGribDataGrid("current", "v-component_of_current_depth_below_sea")
            val strom = if (currentU != null && currentV != null) {
                currentU.copy(
                    uValues = currentU.values,
                    vValues = currentV.values,
                    currentSpeed = CalculateUtil.calculateSpeedFromUV(currentU.values[0].toDouble(), currentV.values[0].toDouble()),
                    currentDirection = CalculateUtil.calculateDirectionFromUV(currentU.values[0].toDouble(), currentV.values[0].toDouble())
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