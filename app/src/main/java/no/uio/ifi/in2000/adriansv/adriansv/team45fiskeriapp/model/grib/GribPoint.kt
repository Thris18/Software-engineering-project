package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.grib

data class GribPoint(
    val latitude: Double,
    val longitude: Double,
    val data: GribData? = null
)