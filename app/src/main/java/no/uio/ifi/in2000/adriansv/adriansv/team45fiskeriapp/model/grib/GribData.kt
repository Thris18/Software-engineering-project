package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib

data class GribData(
    val values: FloatArray,
    val uValues: FloatArray? = null,  // U-komponenter for wind/current
    val vValues: FloatArray? = null,  // V-komponenter for wind/current
    val width: Int,
    val height: Int,
    val latitudes: FloatArray,
    val longitudes: FloatArray,
    val minValue: Float,
    val maxValue: Float,
    val variableName: String,
    val unit: String,
    val referenceTime: String,
    val temperature: Float? = null,
    val windSpeed: Float? = null,
    val windDirection: Float? = null,
    val waveHeight: Float? = null,
    val waveDirection: Float? = null,
    val currentSpeed: Float? = null,
    val currentDirection: Float? = null,
    val pressure: Float? = null,
    val precipitation: Float? = null,
    val lat: Float? = null,
    val lon: Float? = null,
    val time: String? = null,
    val reftime: String? = null,
    val heightAboveGround: Float? = null,
    val heightAboveGround1: Float? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GribData

        if (!values.contentEquals(other.values)) return false
        if (uValues != null) {
            if (other.uValues == null) return false
            if (!uValues.contentEquals(other.uValues)) return false
        } else if (other.uValues != null) return false
        if (vValues != null) {
            if (other.vValues == null) return false
            if (!vValues.contentEquals(other.vValues)) return false
        } else if (other.vValues != null) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (!latitudes.contentEquals(other.latitudes)) return false
        if (!longitudes.contentEquals(other.longitudes)) return false
        if (minValue != other.minValue) return false
        if (maxValue != other.maxValue) return false
        if (variableName != other.variableName) return false
        if (unit != other.unit) return false
        if (referenceTime != other.referenceTime) return false
        if (temperature != other.temperature) return false
        if (windSpeed != other.windSpeed) return false
        if (windDirection != other.windDirection) return false
        if (waveHeight != other.waveHeight) return false
        if (waveDirection != other.waveDirection) return false
        if (currentSpeed != other.currentSpeed) return false
        if (currentDirection != other.currentDirection) return false
        if (pressure != other.pressure) return false
        if (precipitation != other.precipitation) return false
        if (lat != other.lat) return false
        if (lon != other.lon) return false
        if (time != other.time) return false
        if (reftime != other.reftime) return false
        if (heightAboveGround != other.heightAboveGround) return false
        if (heightAboveGround1 != other.heightAboveGround1) return false

        return true
    }

    override fun hashCode(): Int {
        var result = values.contentHashCode()
        result = 31 * result + (uValues?.contentHashCode() ?: 0)
        result = 31 * result + (vValues?.contentHashCode() ?: 0)
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + latitudes.contentHashCode()
        result = 31 * result + longitudes.contentHashCode()
        result = 31 * result + minValue.hashCode()
        result = 31 * result + maxValue.hashCode()
        result = 31 * result + variableName.hashCode()
        result = 31 * result + unit.hashCode()
        result = 31 * result + referenceTime.hashCode()
        result = 31 * result + (temperature?.hashCode() ?: 0)
        result = 31 * result + (windSpeed?.hashCode() ?: 0)
        result = 31 * result + (windDirection?.hashCode() ?: 0)
        result = 31 * result + (waveHeight?.hashCode() ?: 0)
        result = 31 * result + (waveDirection?.hashCode() ?: 0)
        result = 31 * result + (currentSpeed?.hashCode() ?: 0)
        result = 31 * result + (currentDirection?.hashCode() ?: 0)
        result = 31 * result + (pressure?.hashCode() ?: 0)
        result = 31 * result + (precipitation?.hashCode() ?: 0)
        result = 31 * result + (lat?.hashCode() ?: 0)
        result = 31 * result + (lon?.hashCode() ?: 0)
        result = 31 * result + (time?.hashCode() ?: 0)
        result = 31 * result + (reftime?.hashCode() ?: 0)
        result = 31 * result + (heightAboveGround?.hashCode() ?: 0)
        result = 31 * result + (heightAboveGround1?.hashCode() ?: 0)
        return result
    }
}