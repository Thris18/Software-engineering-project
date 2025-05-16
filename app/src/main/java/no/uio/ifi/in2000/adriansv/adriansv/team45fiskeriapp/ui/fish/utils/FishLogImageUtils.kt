package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.fish.utils

import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.net.toUri
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.core.utils.ImageUtils
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.fish.FishLog
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import java.time.LocalDateTime

object FishLogImageUtils {
    fun loadImage(
        context: Context,
        style: Style,
        fishLog: FishLog,
        loadedImages: Set<String>,
        failedImageLoads: Set<String>,
        fishingTripName: String,
        isFishingTripActive: Boolean,
        fishingTripStartTime: LocalDateTime?,
        onImageLoaded: (String) -> Unit,
        onImageLoadFailed: (String) -> Unit
    ) {
        val imageId = "fish_${fishLog.timestamp}"
        
        // Skip if already loaded or failed
        if (imageId in loadedImages || imageId in failedImageLoads) {
            return
        }

        try {
            val uri = fishLog.imageUri?.toUri()
            val image = if (uri?.scheme == "file") {
                // Handle EXIF rotation for files
                val exif = android.media.ExifInterface(uri.path!!)
                val orientation = exif.getAttributeInt(
                    androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
                )
                val bitmap = BitmapFactory.decodeFile(uri.path)
                ImageUtils.rotateBitmap(bitmap, orientation)
            } else {
                // Handle EXIF rotation for content URIs
                uri?.let { context.contentResolver.openInputStream(it) }?.use { stream ->
                    val exif = android.media.ExifInterface(stream)
                    val orientation = exif.getAttributeInt(
                        androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
                        androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
                    )
                    stream.reset() // Reset stream for bitmap decoding
                    val bitmap = BitmapFactory.decodeStream(stream)
                    ImageUtils.rotateBitmap(bitmap, orientation)
                }
            }
            
            if (image != null) {
                try {
                    // Add the image
                    style.addImage(imageId, image)
                    onImageLoaded(imageId)
                    
                    // Add GeoJSON source
                    val source = GeoJsonSource(
                        "fish_${fishLog.timestamp}",
                        "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[${fishLog.longitude},${fishLog.latitude}]},\"properties\":{\"timestamp\":\"${fishLog.timestamp}\"}}"
                    )
                    style.addSource(source)

                    // Determine size based on whether the image is from a fishing trip or fish log
                    val isFromFishingTrip = fishLog.area == fishingTripName || 
                        (isFishingTripActive && fishLog.timestamp.time >= (fishingTripStartTime?.toEpochSecond(java.time.ZoneOffset.UTC) ?: 0L) * 1000) ||
                        (fishLog.description.contains("Fanget under fisketur"))

                    val iconSize = if (isFromFishingTrip) {
                        0.40f  // Adjusted size for fishing trip images
                    } else {
                        0.05f // Keep original size for fish log images
                    }
                    
                    // Add symbol layer
                    val layer = SymbolLayer("fish_${fishLog.timestamp}", "fish_${fishLog.timestamp}")
                        .withProperties(
                            PropertyFactory.iconImage(imageId),
                            PropertyFactory.iconSize(iconSize),
                            PropertyFactory.iconAllowOverlap(true),
                            PropertyFactory.iconIgnorePlacement(true),
                            PropertyFactory.iconAnchor(Property.ICON_ANCHOR_CENTER),
                            PropertyFactory.iconOpacity(0.8f)
                        )
                    style.addLayer(layer)
                } catch (e: Exception) {
                    onImageLoadFailed(imageId)
                }
            }
        } catch (e: Exception) {
            onImageLoadFailed(imageId)
        }
    }
} 