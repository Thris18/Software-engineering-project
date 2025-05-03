package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather

data class WeatherResponse(
    val type: String,
    val geometry: Geometry,
    val properties: Properties
)

data class Geometry(
    val type: String,
    val coordinates: List<Double>
)

data class Properties(
    val meta: Meta,
    val timeseries: List<TimeSeriesEntry>
)

data class Meta(
    val updated_at: String,
    val units: Units
)

data class Units(
    val air_pressure_at_sea_level: String,
    val air_temperature: String,
    val cloud_area_fraction: String,
    val precipitation_amount: String,
    val relative_humidity: String,
    val wind_from_direction: String,
    val wind_speed: String
)

data class TimeSeriesEntry(
    val time: String,
    val data: WeatherData
)

data class WeatherData(
    val instant: Instant,
    val next_1_hours: NextHours? = null,
    val next_6_hours: NextHours? = null,
    val next_12_hours: NextHours? = null
)

data class Instant(
    val details: WeatherDetails
)

data class WeatherDetails(
    val air_pressure_at_sea_level: Double,
    val air_temperature: Double,
    val cloud_area_fraction: Double,
    val relative_humidity: Double,
    val wind_from_direction: Double,
    val wind_speed: Double
)

data class NextHours(
    val summary: WeatherSummary,
    val details: NextHoursDetails
)

data class WeatherSummary(
    val symbol_code: String
)

data class NextHoursDetails(
    val precipitation_amount: Double = 0.0
)


