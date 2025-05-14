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

private const val TAG = "GribRepository"

interface GribRepository {
    suspend fun getGribData(point: GribPoint): GribData?
    suspend fun getGribDataGrid(type: String, variableName: String? = null): GribData?
    suspend fun getAllWeatherGrids(): Map<String, GribData?>

    class GribRepositoryImpl(context: Context) : GribRepository {
        private val gribDataSource = GribDataSource(context)

        override suspend fun getGribData(point: GribPoint): GribData? = withContext(Dispatchers.IO) {
            try {
                // Hent værvarsel data
                val weatherFile = gribDataSource.downloadGribFile("weather")
                if (weatherFile == null) {
                    Log.e(TAG, "Kunne ikke laste ned weather GRIB-fil")
                    return@withContext null
                }
                val weatherData = GribParser.parseGribFile(weatherFile) ?: return@withContext null

                // Hent trykk data
                val pressureData = GribParser.parseGribFile(weatherFile, "Pressure_height_above_ground")
                    ?: return@withContext null
                val pressure = getValueAtCoordinates(pressureData, point.latitude, point.longitude)

                // Hent vind data (u og v komponenter)
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

                // Beregn vindhastighet og retning
                val windSpeed = if (uWind != null && vWind != null) {
                    sqrt(uWind * uWind + vWind * vWind)
                } else null

                val windDirection = if (uWind != null && vWind != null) {
                    // Beregn retning i grader (0-360)
                    val direction = Math.toDegrees(atan2(vWind.toDouble(), uWind.toDouble()))
                    // Konverter til meteorologisk retning (hvor vinden kommer fra)
                    ((direction + 180) % 360).toFloat()
                } else null

                Log.d(TAG, "Vind data:")
                Log.d(TAG, "  u-komponent: $uWind")
                Log.d(TAG, "  v-komponent: $vWind")
                Log.d(TAG, "  Hastighet: $windSpeed")
                Log.d(TAG, "  Retning: $windDirection")

                // Hent nedbør data
                val precipitationData = GribParser.parseGribFile(
                    weatherFile,
                    "Total_precipitation_height_above_ground"
                ) ?: return@withContext null
                val precipitation = getValueAtCoordinates(precipitationData, point.latitude, point.longitude)

                // Hent høyde data
                val heightData = GribParser.parseGribFile(weatherFile, "height_above_ground") ?: return@withContext null
                val height = getValueAtCoordinates(heightData, point.latitude, point.longitude)

                val height1Data = GribParser.parseGribFile(weatherFile, "height_above_ground1") ?: return@withContext null
                val height1 = getValueAtCoordinates(height1Data, point.latitude, point.longitude)

                // Hent tidspunkt data
                val timeData = GribParser.parseGribFile(weatherFile, "time") ?: return@withContext null
                val time = getValueAtCoordinates(timeData, point.latitude, point.longitude)?.toString() ?: "0"

                val reftimeData = GribParser.parseGribFile(weatherFile, "reftime") ?: return@withContext null
                val reftime = getValueAtCoordinates(reftimeData, point.latitude, point.longitude)?.toString() ?: "0"

                // Hent strøm data
                val currentFile = gribDataSource.downloadGribFile("current")
                if (currentFile == null) {
                    Log.e(TAG, "Kunne ikke laste ned current GRIB-fil")
                    return@withContext null
                }

                // Hent u- og v-komponenter for strøm
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

                // Beregn strømhastighet og retning
                val currentSpeed = if (uCurrent != null && vCurrent != null) {
                    sqrt(uCurrent * uCurrent + vCurrent * vCurrent)
                } else null

                val currentDirection = if (uCurrent != null && vCurrent != null) {
                    // Beregn retning i grader (0-360)
                    val direction = Math.toDegrees(atan2(vCurrent.toDouble(), uCurrent.toDouble()))
                    // Konverter til meteorologisk retning (hvor strømmen går)
                    ((direction + 180) % 360).toFloat()
                } else null

                Log.d(TAG, "Strøm data:")
                Log.d(TAG, "  u-komponent: $uCurrent")
                Log.d(TAG, "  v-komponent: $vCurrent")
                Log.d(TAG, "  Hastighet: $currentSpeed")
                Log.d(TAG, "  Retning: $currentDirection")

                // Hent bølge data
                val wavesFile = gribDataSource.downloadGribFile("waves")
                if (wavesFile == null) {
                    Log.e(TAG, "Kunne ikke laste ned waves GRIB-fil")
                    return@withContext null
                }
                val wavesData = GribParser.parseGribFile(wavesFile) ?: return@withContext null
                val waveHeight = getValueAtCoordinates(wavesData, point.latitude, point.longitude)
                val waveDirection = getValueAtCoordinates(wavesData, point.latitude, point.longitude)

                // Opprett nytt GribData-objekt med alle verdiene
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
                Log.e(TAG, "Feil ved henting av GRIB-data: ${e.message}", e)
                null
            }
        }

        private fun getValueAtCoordinates(gribData: GribData, latitude: Double, longitude: Double): Float? {
            try {
                Log.d(TAG, "Henter verdi ved koordinater: $latitude, $longitude")

                var closestLatIndex = -1
                var closestLonIndex = -1
                var minLatDiff = Double.MAX_VALUE
                var minLonDiff = Double.MAX_VALUE

                val minLat = gribData.latitudes.minOrNull() ?: 0f
                val maxLat = gribData.latitudes.maxOrNull() ?: 0f
                val minLon = gribData.longitudes.minOrNull() ?: 0f
                val maxLon = gribData.longitudes.maxOrNull() ?: 0f

                Log.d(TAG, "Latitude range: $minLat til $maxLat")
                Log.d(TAG, "Longitude range: $minLon til $maxLon")

                if (latitude < minLat || latitude > maxLat || longitude < minLon || longitude > maxLon) {
                    Log.w(TAG, "Koordinater utenfor data grid grensene")
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
                        Log.w(TAG, "Koordinater for langt fra datagridpunkter. Lat differanse: $minLatDiff, Lon differanse: $minLonDiff")
                        return (gribData.minValue + gribData.maxValue) / 2
                    }

                    val index = closestLatIndex * gribData.width + closestLonIndex

                    if (index < gribData.values.size) {
                        val value = gribData.values[index]
                        Log.d(TAG, "Fant verdi $value på indeks $index")

                        if (value.isNaN()) {
                            Log.w(TAG, "NaN verdi funnet på indeks $index, søker etter valide naboverdier")

                            for (radius in 1..3) {
                                val validNeighbor = findValidNeighborValue(gribData, closestLatIndex, closestLonIndex, radius)
                                if (validNeighbor != null) {
                                    Log.d(TAG, "Funnet valid naboverdi $validNeighbor på radius $radius")
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
                Log.e(TAG, "Feil ved henting av verdi på koordinat: ${e.message}", e)
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
            // Hent vind-data med både u- og v-komponenter
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

            // Hent strøm-data med både u- og v-komponenter
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