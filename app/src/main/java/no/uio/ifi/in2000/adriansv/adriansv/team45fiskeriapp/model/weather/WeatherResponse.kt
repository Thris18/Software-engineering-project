package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather

// Data classes for å representere værdata
data class WeatherResponse(
    val properties: Properties
)

data class Properties(
    val timeseries: List<TimeSeriesEntry>
)

data class TimeSeriesEntry(
    val time: String,
    val weatherInfo: WeatherInfo
)

data class WeatherData(
    val instant: InstantDetails,
    val next_1_hours: NextHours? = null
)

data class InstantDetails(
    val details: WeatherDetails
)

data class WeatherDetails(
    val air_temperature: Double,
    val cloud_area_fraction: Double,
    val relative_humidity: Double,
    val wind_from_direction: Double,
    val wind_speed: Double
)

data class NextHours(
    val summary: WeatherSummary
)

data class WeatherSummary(
    val symbol_code: String
)


