package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.map.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "MapScreenshotUtils"

object MapScreenshotUtils {
    fun saveMapScreenshot(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "map_screenshot_$timestamp.jpg"
            val file = File(context.filesDir, fileName)
            
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            
            Uri.fromFile(file)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving map screenshot", e)
            null
        }
    }
} 