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


// Denne filen inneholder GribParser-klassen, som har ansvar for å lese og tolke GRIB-filer (gridded binary) med vær- og havdata.
// Klassen åpner en GRIB-fil, henter ut relevante variabler (f.eks. vind, bølger, temperatur), og konverterer dataene til et GribData-objekt
// som kan brukes videre i appen. Parseren håndterer ulike dimensjoner og variabelnavn, og forsøker å finne de mest relevante dataene automatisk.
// Den støtter også feilhåndtering og tilpasser seg hvis enkelte data mangler eller har uventet format.

class GribParser {
    companion object {
        private const val TAG = "GribParser"

        fun parseGribFile(gribFile: File, variableName: String? = null): GribData? {
            try {

                // Open Gribfile
                val dataset = NetcdfDatasets.openDataset(gribFile.absolutePath)

                val latVar = dataset.findVariable("lat") ?: dataset.findVariable("latitude")
                val lonVar = dataset.findVariable("lon") ?: dataset.findVariable("longitude")

                if (latVar == null || lonVar == null) {
                    dataset.close()
                    return null
                }

                val latArray = latVar.read()
                val lonArray = lonVar.read()

                val latDims = latVar.shape
                val lonDims = lonVar.shape

                val height = latDims[0]
                val width = lonDims[0]


                val latitudes = FloatArray(height)
                val longitudes = FloatArray(width)

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


                val dataVar = if (variableName != null) {
                    dataset.findVariable(variableName) ?: run {
                        Log.e(TAG, "Kunne ikke finne variabel: $variableName")
                        dataset.close()
                        return null
                    }
                } else {

                    val dataVars = dataset.variables.filter {
                        !it.shortName.equals("lat", ignoreCase = true) &&
                                !it.shortName.equals("lon", ignoreCase = true) &&
                                !it.shortName.equals("latitude", ignoreCase = true) &&
                                !it.shortName.equals("longitude", ignoreCase = true) &&
                                !it.shortName.equals("time", ignoreCase = true) &&
                                !it.shortName.equals("reftime", ignoreCase = true) &&
                                it.shape.size >= 2
                    }

                    if (dataVars.isEmpty()) {
                        dataset.close()
                        return null
                    }

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
                    dataVars.find { varName ->
                        preferredVars.any { preferred -> varName.shortName.contains(preferred, ignoreCase = true) }
                    } ?: dataVars.first()
                }
                val varName = dataVar.shortName
                val unit = dataVar.unitsString ?: ""


                val referenceTime = if (varName.equals("time", ignoreCase = true) || varName.equals("reftime", ignoreCase = true)) {
                    val timeArray = dataVar.read()
                    val timeIdx = Index.factory(timeArray.shape)
                    timeIdx.set(0)
                    val timeValue = timeArray.getDouble(timeIdx)
                    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                    formatter.format(Date((timeValue * 1000).toLong()))
                } else {
                    val timeVar = dataset.findVariable("time")
                    if (timeVar != null) {
                        val timeArray = timeVar.read()
                        val timeIdx = Index.factory(timeArray.shape)
                        timeIdx.set(0)
                        val timeValue = timeArray.getDouble(timeIdx)
                        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                        formatter.format(Date((timeValue * 1000).toLong()))
                    } else {
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())
                    }
                }


                // Reads the data
                val dataArray = dataVar.read()
                val values = FloatArray(width * height)
                var minValue = Float.MAX_VALUE
                var maxValue = Float.MIN_VALUE

                var nanCount = 0
                var validCount = 0

                try {
                    val shape = dataVar.shape
                    val dimCount = shape.size


                    // Special handling for 1D-variables
                    if (dimCount == 1) {

                        val value = when (dataVar.dataType) {
                            ucar.ma2.DataType.DOUBLE -> dataArray.getDouble(0).toFloat()
                            ucar.ma2.DataType.FLOAT -> dataArray.getFloat(0)
                            ucar.ma2.DataType.INT -> dataArray.getInt(0).toFloat()
                            else -> dataArray.getFloat(0)
                        }

                        for (i in values.indices) {
                            values[i] = value
                            if (!value.isNaN()) {
                                minValue = min(minValue, value)
                                maxValue = max(maxValue, value)
                                validCount++
                            } else {
                                nanCount++
                            }
                        }
                    } else {
                        if (dimCount < 2) {
                            throw IllegalArgumentException("Dataarrayet har for få dimensjoner")
                        }


                        var index = 0
                        for (y in 0 until height) {
                            for (x in 0 until width) {
                                try {
                                    val idx = Index.factory(shape)

                                    when (dimCount) {
                                        2 -> {

                                            idx.setDim(0, y)
                                            idx.setDim(1, x)
                                        }
                                        3 -> {
                                            // 3D data (time, lat, lon)
                                            idx.setDim(0, 0)
                                            idx.setDim(1, y)
                                            idx.setDim(2, x)
                                        }
                                        else -> {
                                            // 4D eller mer (time, level, lat, lon)
                                            for (d in 0 until dimCount - 2) {
                                                idx.setDim(d, 0)
                                            }
                                            idx.setDim(dimCount - 2, y)  // latitude
                                            idx.setDim(dimCount - 1, x)  // longitude
                                        }
                                    }

                                    val value = when (dataVar.dataType) {
                                        ucar.ma2.DataType.DOUBLE -> dataArray.getDouble(idx).toFloat()
                                        ucar.ma2.DataType.FLOAT -> dataArray.getFloat(idx)
                                        ucar.ma2.DataType.INT -> dataArray.getInt(idx).toFloat()
                                        else -> dataArray.getFloat(idx)
                                    }

                                    values[index] = value

                                    if (!value.isNaN()) {
                                        minValue = min(minValue, value)
                                        maxValue = max(maxValue, value)
                                        validCount++
                                    } else {
                                        nanCount++
                                    }
                                } catch (e: Exception) {
                                    values[index] = 0f
                                    nanCount++
                                }
                                index++
                            }
                        }
                    }
                } catch (e: Exception) {

                    try {
                        for (i in values.indices) {
                            values[i] = 0f
                        }

                        val totalSize = dataArray.size.toInt()
                        val valueCount = min(width * height, totalSize)

                        for (i in 0 until valueCount) {
                            try {
                                val flatIndex = i % totalSize
                                val value = dataArray.getFloat(flatIndex)

                                if (!value.isNaN()) {
                                    values[i] = value
                                    minValue = min(minValue, value)
                                    maxValue = max(maxValue, value)
                                    validCount++
                                } else {
                                    nanCount++
                                }
                            } catch (_: Exception) {
                            }
                        }
                    } catch (_: Exception) {
                    }
                }

                dataset.close()

                // Setter verdier hvis all data er NaN
                if (minValue == Float.MAX_VALUE || maxValue == Float.MIN_VALUE) {
                    minValue = 0f
                    maxValue = 1f
                }

                return GribData(
                    values = values,
                    width = width,
                    height = height,
                    latitudes = latitudes,
                    longitudes = longitudes,
                    minValue = minValue,
                    maxValue = maxValue,
                    variableName = varName,
                    unit = unit,
                    referenceTime = referenceTime
                )
            } catch (e: Exception) {
                return null
            }
        }
    }
}