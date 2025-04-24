package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.grib

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib.GribData
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib.GribPoint
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class GribRepository(private val context: Context) {
    private val TAG = "GribRepository"
    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun getGribData(point: GribPoint): GribData? = withContext(Dispatchers.IO) {
        try {
            // Hent værvarsel data
            val weatherFile = downloadGribFile("weather")
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
            val time = timeData.referenceTime

            val reftimeData = GribParser.parseGribFile(weatherFile, "reftime") ?: return@withContext null
            val reftime = reftimeData.referenceTime

            // Hent strøm data
            val currentFile = downloadGribFile("current")
            if (currentFile == null) {
                Log.e(TAG, "Kunne ikke laste ned current GRIB-fil")
                return@withContext null
            }
            val currentData = GribParser.parseGribFile(currentFile) ?: return@withContext null
            val currentSpeed = getValueAtCoordinates(currentData, point.latitude, point.longitude)
            val currentDirection = getValueAtCoordinates(currentData, point.latitude, point.longitude)

            // Hent bølge data
            val wavesFile = downloadGribFile("waves")
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
                windSpeed = uWind,
                windDirection = vWind,
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
                height_above_ground = height ?: 0f,
                height_above_ground1 = height1 ?: 0f
            )
        } catch (e: Exception) {
            Log.e(TAG, "Feil ved henting av GRIB-data: ${e.message}", e)
            null
        }
    }

    private suspend fun downloadGribFile(contentType: String): File? = withContext(Dispatchers.IO) {
        try {
            // Oppdater URL for å bruke riktig API-endepunkt og parametere
            val url = when (contentType) {
                "weather" -> "https://api.met.no/weatherapi/gribfiles/1.1/?area=oslofjord&content=weather"
                "current" -> "https://api.met.no/weatherapi/gribfiles/1.1/?area=oslofjord&content=current"
                "waves" -> "https://api.met.no/weatherapi/gribfiles/1.1/?area=oslofjord&content=waves"
                else -> return@withContext null
            }

            Log.d(TAG, "Laster ned GRIB-fil fra URL: $url")

            val gribDir = File(context.filesDir, "grib_files")
            if (!gribDir.exists()) {
                gribDir.mkdirs()
            }

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "BtApp/1.0 (https://github.com/carlorr/BtApp)")
                .header("Accept", "application/x-grib")
                .header("Accept-Encoding", "gzip")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Feilet ved nedlastning av GRIB-fil: ${response.code}")
                    Log.e(TAG, "Response body: ${response.body?.string()}")
                    return@withContext null
                }

                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "oslofjord_${contentType}_$timestamp.grb"
                val file = File(gribDir, fileName)

                response.body?.let { body ->
                    FileOutputStream(file).use { outputStream ->
                        body.byteStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }

                Log.d(TAG, "GRIB-fil lagret: ${file.absolutePath}")
                file
            }
        } catch (e: Exception) {
            Log.e(TAG, "Feil ved nedlasting av GRIB-fil: ${e.message}", e)
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
}