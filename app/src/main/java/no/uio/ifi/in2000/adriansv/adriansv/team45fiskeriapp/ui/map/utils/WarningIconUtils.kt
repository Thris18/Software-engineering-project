package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.map.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import org.maplibre.android.maps.Style
import java.io.InputStream

private const val TAG = "WarningIconUtils"

object WarningIconUtils {
    fun loadWarningIcons(context: Context, style: Style) {
        val iconTypes = listOf(
            "wind", "rain", "snow", "lightning", "avalanches", "generic",
            "polarlow", "stormsurge", "flood", "forestfire", "ice",
            "rainflood", "drivingconditions", "landslide"
        )
        val severities = listOf("red", "orange", "yellow")

        var loadedIcons = 0
        var failedIcons = 0

        for (type in iconTypes) {
            for (severity in severities) {
                try {
                    val iconPath = "png/icon-warning-$type-$severity.png"
                    val inputStream: InputStream = context.assets.open(iconPath)
                    inputStream.use { stream ->
                        val bitmap = BitmapFactory.decodeStream(stream)
                        if (bitmap != null) {
                            val iconId = "$type-$severity"
                            style.addImage(iconId, bitmap)
                            loadedIcons++
                        } else {
                            failedIcons++
                            Log.e(TAG, "Failed to decode bitmap for icon: $type-$severity")
                        }
                    }
                } catch (_: Exception) {
                    failedIcons++
                }
            }
        }

        // Load extreme warning icon
        try {
            val inputStream: InputStream = context.assets.open("png/icon-warning-extreme.png")
            inputStream.use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream)
                if (bitmap != null) {
                    style.addImage("extreme", bitmap)
                    loadedIcons++
                } else {
                    failedIcons++
                    Log.e(TAG, "Failed to decode extreme icon bitmap")
                }
            }
        } catch (e: Exception) {
            failedIcons++
            Log.e(TAG, "Failed to load extreme icon", e)
        }
    }
} 