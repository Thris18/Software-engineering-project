package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib

// Representerer data som er hentet fra GRIB-fil

data class GribData(
    val values: FloatArray,
    val width: Int,
    val height: Int,
    val latitudes: FloatArray,
    val longitudes: FloatArray,
    val minValue: Float,
    val maxValue: Float,
    val variableName: String,
    val unit: String,
    val referenceTime: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GribData

        if (!values.contentEquals(other.values)) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (!latitudes.contentEquals(other.latitudes)) return false
        if (!longitudes.contentEquals(other.longitudes)) return false
        if (minValue != other.minValue) return false
        if (maxValue != other.maxValue) return false
        if (variableName != other.variableName) return false
        if (unit != other.unit) return false
        if (referenceTime != other.referenceTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = values.contentHashCode()
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + latitudes.contentHashCode()
        result = 31 * result + longitudes.contentHashCode()
        result = 31 * result + minValue.hashCode()
        result = 31 * result + maxValue.hashCode()
        result = 31 * result + variableName.hashCode()
        result = 31 * result + unit.hashCode()
        result = 31 * result + referenceTime.hashCode()
        return result
    }
}


// Mer UI-vennlig modell som representerer en lokasjon fra GRIB-dataverdien
data class GribPoint(
    val latitude: Double,
    val longitude: Double,
    val value: Float,
    val variableName: String,
    val unit: String
)


// Metadata for en GRIB-fil

data class GribFileMetadata(
    val filename: String,
    val variableName: String,
    val referenceTime: String,
    val forecastTime: String,
    val area: String,
    val path: String
)