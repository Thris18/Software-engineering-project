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
    val updatedAt: String,
    val units: Units
)

data class Units(
    val airPressureAtSeaLevel: String,
    val airTemperature: String,
    val cloudAreaFraction: String,
    val precipitationAmount: String,
    val relativeHumidity: String,
    val windFromDirection: String,
    val windSpeed: String
)

data class TimeSeriesEntry(
    val time: String,
    val data: WeatherData
)

data class WeatherData(
    val instant: Instant,
    val next1hours: NextHours? = null,
    val next6hours: NextHours? = null,
    val next12hours: NextHours? = null
)

data class Instant(
    val details: WeatherDetails
)

data class WeatherDetails(
    val airPressureAtSeaLevel: Double,
    val airTemperature: Double,
    val cloudAreaFraction: Double,
    val relativeHumidity: Double,
    val windFromDirection: Double,
    val windSpeed: Double
)

data class NextHours(
    val summary: WeatherSummary,
    val details: NextHoursDetails
)

data class WeatherSummary(
    val symbolCode: String
)

data class NextHoursDetails(
    val precipitationAmount: Double = 0.0
)


