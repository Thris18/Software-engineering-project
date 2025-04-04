package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.grib

import android.util.Log
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib.GribData
import ucar.nc2.dataset.NetcdfDatasets
import ucar.ma2.Index
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

// Parser for GRIB-filene som bruker NetCDF-biblioteket

class GribParser {
    companion object {
        private const val TAG = "GribParser"

        fun parseGribFile(gribFile: File): GribData? {
            try {
                Log.d(TAG, "Parser GRIB-fil: ${gribFile.absolutePath}")

                // Åpner gribfilen
                val dataset = NetcdfDatasets.openDataset(gribFile.absolutePath)

                // Debug: Printer alle variablene i datasettet
                Log.d(TAG, "Variabler i datasett: ${dataset.variables.joinToString(", ") { it.shortName }}")

                // Henter koordinater (latitude and longitude)
                val latVar = dataset.findVariable("lat") ?: dataset.findVariable("latitude")
                val lonVar = dataset.findVariable("lon") ?: dataset.findVariable("longitude")

                if (latVar == null || lonVar == null) {
                    Log.e(TAG, "Kunne ikke finne lat/lon variabler i GRIB-filer")
                    dataset.close()
                    return null
                }

                val latArray = latVar.read()
                val lonArray = lonVar.read()

                // Henter dimensjoner
                val latDims = latVar.shape
                val lonDims = lonVar.shape

                val height = latDims[0]
                val width = lonDims[0]

                Log.d(TAG, "Grid dimensjoner: $width x $height")

                val latitudes = FloatArray(height)
                val longitudes = FloatArray(width)

                // Henter latitude og longitude verdier ved å bruke index
                for (i in 0 until height) {
                    val idx = Index.factory(latArray.shape)
                    idx.set(i)
                    latitudes[i] = latArray.getFloat(idx)
                }

                for (i in 0 until width) {
                    val idx = Index.factory(lonArray.shape)
                    idx.set(i)
                    longitudes[i] = lonArray.getFloat(idx)
                }

                val latRange = if (latitudes.isNotEmpty()) "${latitudes.minOrNull()} to ${latitudes.maxOrNull()}" else "empty"
                val lonRange = if (longitudes.isNotEmpty()) "${longitudes.minOrNull()} to ${longitudes.maxOrNull()}" else "empty"
                Log.d(TAG, "Latitude range: $latRange")
                Log.d(TAG, "Longitude range: $lonRange")

                // Finner første datavariabel
                val dataVars = dataset.variables.filter {
                    !it.shortName.equals("lat", ignoreCase = true) &&
                            !it.shortName.equals("lon", ignoreCase = true) &&
                            !it.shortName.equals("latitude", ignoreCase = true) &&
                            !it.shortName.equals("longitude", ignoreCase = true) &&
                            !it.shortName.equals("time", ignoreCase = true) &&
                            !it.shortName.equals("reftime", ignoreCase = true) &&
                            it.shape.size >= 2  // Using shape.size instead of rank()
                }

                Log.d(TAG, "Funnet potensielle data variabler: ${dataVars.joinToString(", ") { it.shortName }}")

                if (dataVars.isEmpty()) {
                    Log.e(TAG, "Fant ikke data variabler i GRIB-filen som passer")
                    dataset.close()
                    return null
                }

                // Prøver å finne variabler som faktisk har data
                val preferredVars = listOf(
                    "Pressure_height_above_ground",
                    "Wind_speed_gust",
                    "Significant_height_of_wind_waves",
                    "Temperature",
                    "u-component_of_wind",
                    "v-component_of_wind",
                    "u-component_of_current",
                    "v-component_of_current",
                    "Total_precipitation"
                )
                val dataVar = dataVars.find { varName ->
                    preferredVars.any { preferred -> varName.shortName.contains(preferred, ignoreCase = true) }
                } ?: dataVars.first()

                // Henter variabelnavn og enhet
                val variableName = dataVar.shortName
                val unit = dataVar.unitsString ?: ""

                // Logger detaljer rundt variabler
                Log.d(TAG, "Bruker data variabel: $variableName (${dataVar.dataType}, ${dataVar.shape.joinToString("x")})")
                Log.d(TAG, "Variabelenhet: $unit")

                // Henter referansetid
                val timeVar = dataset.findVariable("time")
                val referenceTime = if (timeVar != null) {
                    val timeArray = timeVar.read()
                    val timeIdx = Index.factory(timeArray.shape)
                    timeIdx.set(0)
                    val timeValue = timeArray.getDouble(timeIdx)
                    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                    formatter.format(Date((timeValue * 1000).toLong()))
                } else {
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())
                }

                Log.d(TAG, "Referansetid: $referenceTime")

                // Les dataen
                val dataArray = dataVar.read()
                val values = FloatArray(width * height)
                var minValue = Float.MAX_VALUE
                var maxValue = Float.MIN_VALUE

                // Variabler for hvor mange Not A Number det er
                var nanCount = 0
                var validCount = 0

                // Håndterer forskjellige dimensjoner
                try {
                    // Bestemmer hvilken dimensjon basert på form
                    val shape = dataVar.shape
                    val dimCount = shape.size

                    if (dimCount >= 4) {
                        // Eksempel: (time, level, lat, lon) eller (ensemble, time, lat, lon)
                        var index = 0

                        for (y in 0 until height) {
                            for (x in 0 until width) {
                                try {
                                    val idx = Index.factory(shape)
                                    // Setter første dimensjoner til 0, siste to til y,x
                                    for (d in 0 until dimCount - 2) {
                                        idx.setDim(d, 0)
                                    }
                                    idx.setDim(dimCount - 2, y)  // second-last dimension (lat)
                                    idx.setDim(dimCount - 1, x)  // last dimension (lon)

                                    val value = dataArray.getFloat(idx)
                                    values[index] = value

                                    if (!value.isNaN()) {
                                        minValue = min(minValue, value)
                                        maxValue = max(maxValue, value)
                                        validCount++
                                    } else {
                                        nanCount++
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error ved lesing av dataverdi for ($x, $y): ${e.message}")
                                    values[index] = 0f
                                    nanCount++
                                }
                                index++
                            }
                        }
                    } else if (dimCount == 3) {
                        // 3D data (time, lat, lon) eller (ensemble, lat, lon)
                        val firstDimIndex = 0  // Bruker første tid eller ensemble slice
                        var index = 0

                        for (y in 0 until height) {
                            for (x in 0 until width) {
                                try {
                                    val idx = Index.factory(shape)
                                    idx.setDim(0, firstDimIndex)
                                    idx.setDim(1, y)
                                    idx.setDim(2, x)

                                    val value = dataArray.getFloat(idx)
                                    values[index] = value

                                    if (!value.isNaN()) {
                                        minValue = min(minValue, value)
                                        maxValue = max(maxValue, value)
                                        validCount++
                                    } else {
                                        nanCount++
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error ved lesning av dataverdi for ($x, $y): ${e.message}")
                                    values[index] = 0f  // Use 0 instead of NaN
                                    nanCount++
                                }
                                index++
                            }
                        }
                    } else {
                        // 2D data (lat, lon)
                        var index = 0

                        for (y in 0 until height) {
                            for (x in 0 until width) {
                                try {
                                    val idx = Index.factory(shape)
                                    idx.setDim(0, y)
                                    idx.setDim(1, x)

                                    val value = dataArray.getFloat(idx)
                                    values[index] = value

                                    if (!value.isNaN()) {
                                        minValue = min(minValue, value)
                                        maxValue = max(maxValue, value)
                                        validCount++
                                    } else {
                                        nanCount++
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error ved lesning av dataverdi for ($x, $y): ${e.message}")
                                    values[index] = 0f  // Bruker 0 istedet for NaN
                                    nanCount++
                                }
                                index++
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error ved lesning av data array: ${e.message}")

                    // Forsøk en fallback approach ved å bruke direkte iterasjon
                    try {
                        // Fyller array med noe meningsfull data
                        for (i in values.indices) {
                            values[i] = 0f  // Standard verdi
                        }

                        // Prøver å hente ut verdier direkte fra arrayet
                        val totalSize = dataArray.size.toInt()
                        val valueCount = min(width * height, totalSize)

                        for (i in 0 until valueCount) {
                            try {
                                val flatIndex = i % totalSize  // Passer på at vi ikke går out of bounds
                                val value = dataArray.getFloat(flatIndex)

                                if (!value.isNaN()) {
                                    values[i] = value
                                    minValue = min(minValue, value)
                                    maxValue = max(maxValue, value)
                                    validCount++
                                } else {
                                    nanCount++
                                }
                            } catch (e2: Exception) {
                                Log.e(TAG, "Error ved lesning: ${e2.message}")
                            }
                        }


                    } catch (e2: Exception) {
                        Log.e(TAG, "Backup-lesemetoden feilet: ${e2.message}")
                    }
                }

                dataset.close()

                // Setter verdier hvis all data er NaN
                if (minValue == Float.MAX_VALUE || maxValue == Float.MIN_VALUE) {
                    minValue = 0f
                    maxValue = 1f
                }

                Log.d(TAG, "Klarte å parse GRIB-fil. Variabel: $variableName, Min: $minValue, Max: $maxValue")
                Log.d(TAG, "Datakvalitet: $validCount valide punkter, $nanCount NaN-verdier ${width * height} totale antall punkter")

                return GribData(
                    values = values,
                    width = width,
                    height = height,
                    latitudes = latitudes,
                    longitudes = longitudes,
                    minValue = minValue,
                    maxValue = maxValue,
                    variableName = variableName,
                    unit = unit,
                    referenceTime = referenceTime
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error ved parsing av GRIB-fil: ${e.message}", e)
                return null
            }
        }
    }
}