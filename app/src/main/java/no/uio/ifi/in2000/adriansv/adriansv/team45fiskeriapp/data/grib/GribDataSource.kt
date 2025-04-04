package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.grib


import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib.GribData
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

class GribDataSource : GribRepository {
    private val TAG = "GribDataSource"

    // OkHttp client for å laste ned GRIB-filer
    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Laster ned GRIB-filen fra Met sitt api
    override suspend fun downloadOslofjordGribFile(context: Context, contentType: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                // Validerer typen
                val validContentTypes = setOf("current", "waves", "weather")
                if (contentType !in validContentTypes) {
                    Log.e(TAG, "Invalid content type: $contentType. Valide types er: $validContentTypes")
                    return@withContext null
                }

                // URL for Oslofjord området
                val url = "https://api.met.no/weatherapi/gribfiles/1.1/?area=oslofjord&content=$contentType"

                Log.d(TAG, "Laster ned GRIB-fil fra URL: $url")

                // Lager mappe for GRIB-filer hvis det ikke finnes
                val gribDir = File(context.filesDir, "grib_files")
                if (!gribDir.exists()) {
                    gribDir.mkdirs()
                }

                // Build request
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "OslofjordGribApp/1.0")
                    .header("Accept", "application/x-grib")
                    .build()

                // Kjør request
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e(TAG, "Feilet ved nedlastning av GRIB-fil: ${response.code}")
                        return@withContext null
                    }

                    // Generer filnavn
                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val fileName = "oslofjord_${contentType}_$timestamp.grb"
                    val file = File(gribDir, fileName)

                    // Lagre fil
                    val responseBody = response.body ?: return@withContext null
                    FileOutputStream(file).use { outputStream ->
                        responseBody.byteStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    Log.d(TAG, "GRIB-fil lagret: ${file.absolutePath}")
                    file
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error ved nedlastning av GRIB-fil: ${e.message}", e)
                null
            }
        }
    }

    // Laster GRIB-data fra lokal fil ved å bruke parseren

    override suspend fun loadGribData(gribFile: File): GribData? {
        return withContext(Dispatchers.IO) {
            try {
                GribParser.parseGribFile(gribFile)
            } catch (e: Exception) {
                Log.e(TAG, "Error ved lasting av GRIB data: ${e.message}", e)
                null
            }
        }
    }

    // Finner verdien til et spesifikt koordinat i GRIB-dataen
    override fun getValueAtCoordinates(gribData: GribData, latitude: Double, longitude: Double): Float? {
        try {
            // Logger input koordinater
            Log.d(TAG, "Henter verdi ved koordinater: $latitude, $longitude")

            // Finner nærmeste latitude og longitude
            var closestLatIndex = -1
            var closestLonIndex = -1
            var minLatDiff = Double.MAX_VALUE
            var minLonDiff = Double.MAX_VALUE

            // Logger tilgjengelige lat/lon ranges
            val minLat = gribData.latitudes.minOrNull() ?: 0f
            val maxLat = gribData.latitudes.maxOrNull() ?: 0f
            val minLon = gribData.longitudes.minOrNull() ?: 0f
            val maxLon = gribData.longitudes.maxOrNull() ?: 0f

            Log.d(TAG, "Latitude range: $minLat til $maxLat")
            Log.d(TAG, "Longitude range: $minLon til $maxLon")

            // Sjekker hvis koordinatene er innenfor minimum grensene
            if (latitude < minLat || latitude > maxLat || longitude < minLon || longitude > maxLon) {
                Log.w(TAG, "Koordinater utenfor data grid grensene")
                return null
            }

            // Finner nærmeste latitude
            for (i in gribData.latitudes.indices) {
                val diff = abs(gribData.latitudes[i] - latitude)
                if (diff < minLatDiff) {
                    minLatDiff = diff
                    closestLatIndex = i
                }
            }

            // Finner nærmeste longitude
            for (i in gribData.longitudes.indices) {
                val diff = abs(gribData.longitudes[i] - longitude)
                if (diff < minLonDiff) {
                    minLonDiff = diff
                    closestLonIndex = i
                }
            }

            // Hvis vi finner valide indices, hent verdien
            if (closestLatIndex >= 0 && closestLonIndex >= 0) {
                // Sjekker hvis koordinatene er relativt nærme (innenfor en halv grad)
                if (minLatDiff > 0.5 || minLonDiff > 0.5) {
                    Log.w(TAG, "Koordinater for langt fra datagridpunkter. Lat differanse: $minLatDiff, Lon differanse: $minLonDiff")
                    return (gribData.minValue + gribData.maxValue) / 2 // Returner midtverdien som fallback
                }

                // Hent grid verdi - bruker riktig index for lat/lon grid
                val index = closestLatIndex * gribData.width + closestLonIndex

                if (index < gribData.values.size) {
                    val value = gribData.values[index]
                    Log.d(TAG, "Fant verdi $value på indeks $index")

                    // Sjekk etter NaN og bytt ut med ordentlig verdi
                    if (value.isNaN()) {
                        Log.w(TAG, "NaN verdi funnet på indeks $index, søker etter valide naboverdier")

                        // Se etter nabo-points med ekspanderende radius
                        for (radius in 1..3) {
                            val validNeighbor = findValidNeighborValue(gribData, closestLatIndex, closestLonIndex, radius)
                            if (validNeighbor != null) {
                                Log.d(TAG, "Funnet valid naboverdi $validNeighbor på radius $radius")
                                return validNeighbor
                            }
                        }

                        // Hvis alle naboer er NaN, returner midtpunkt av rangen som fallback
                        val fallbackValue = (gribData.minValue + gribData.maxValue) / 2
                        return fallbackValue
                    }

                    return value
                } else {
                    return (gribData.minValue + gribData.maxValue) / 2 // Midtverdi som fallback
                }
            } else {
                return null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error ved henting av verdi på koordinat: ${e.message}", e)
            return null
        }
    }


    // Hjelper som finner valid (ikke-NaN) naboverdi innenfor en viss radius
    private fun findValidNeighborValue(gribData: GribData, centerLatIdx: Int, centerLonIdx: Int, radius: Int): Float? {
        val width = gribData.width
        val height = gribData.height
        val values = gribData.values

        // Sjekker alle gridpunkter innenfor radiusen
        for (latOffset in -radius..radius) {
            for (lonOffset in -radius..radius) {
                // Hopp over midtpunkt (allerede sjekket)
                if (latOffset == 0 && lonOffset == 0) continue

                val latIdx = centerLatIdx + latOffset
                val lonIdx = centerLonIdx + lonOffset

                // Sjekk grenser
                if (latIdx >= 0 && latIdx < height && lonIdx >= 0 && lonIdx < width) {
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

    // Konverterer GRIB-data til et bilde for visualisering
    override suspend fun convertGribToImage(gribData: GribData, context: Context): String? {
        return withContext(Dispatchers.IO) {
            try {
                // Lager bitmap
                val bitmap = Bitmap.createBitmap(gribData.width, gribData.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                val paint = Paint()

                // Setter farge basert på verdien
                val valueRange = gribData.maxValue - gribData.minValue

                for (y in 0 until gribData.height) {
                    for (x in 0 until gribData.width) {
                        val index = y * gribData.width + x
                        val value = gribData.values[index]

                        if (!value.isNaN() && valueRange > 0) {
                            val normalizedValue = (value - gribData.minValue) / valueRange
                            paint.color = getColorForValue(normalizedValue)
                        } else {
                            // Transparent for NaN or invalid values
                            paint.color = Color.TRANSPARENT
                        }

                        canvas.drawPoint(x.toFloat(), y.toFloat(), paint)
                    }
                }

                // Lagrer bilde
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val imageFile = File(context.cacheDir, "grib_${gribData.variableName}_$timestamp.png")

                FileOutputStream(imageFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }

                Log.d(TAG, "GRIB bilde lagd: ${imageFile.absolutePath}")
                imageFile.absolutePath
            } catch (e: Exception) {
                Log.e(TAG, "Error ved konvertering av GRIB data til bilde: ${e.message}", e)
                null
            }
        }
    }

    // Hjelpemetode for å skaffe en farge til en normalverdi
    private fun getColorForValue(normalizedValue: Float): Int {
        return when {
            normalizedValue < 0.2f -> Color.rgb(0, 0, 255)    // Blue
            normalizedValue < 0.4f -> Color.rgb(0, 255, 255)  // Cyan
            normalizedValue < 0.6f -> Color.rgb(0, 255, 0)    // Green
            normalizedValue < 0.8f -> Color.rgb(255, 255, 0)  // Yellow
            else -> Color.rgb(255, 0, 0)                     // Red
        }
    }
}