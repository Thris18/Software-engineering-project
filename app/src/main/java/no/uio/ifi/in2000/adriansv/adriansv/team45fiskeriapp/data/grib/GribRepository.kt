package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.grib

import android.content.Context
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib.GribData
import java.io.File

interface GribRepository {
    suspend fun downloadOslofjordGribFile(context: Context, contentType: String): File?

    suspend fun loadGribData(gribFile: File): GribData?

    fun getValueAtCoordinates(gribData: GribData, latitude: Double, longitude: Double): Float?

    suspend fun convertGribToImage(gribData: GribData, context: Context): String?
}