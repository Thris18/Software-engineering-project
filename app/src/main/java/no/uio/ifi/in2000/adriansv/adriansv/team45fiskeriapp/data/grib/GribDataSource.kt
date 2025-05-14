package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.grib

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val TAG = "GribDataSource"


class GribDataSource(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun downloadGribFile(contentType: String): File? = withContext(Dispatchers.IO) {
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
} 