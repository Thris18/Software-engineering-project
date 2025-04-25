package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather

import java.util.*

enum class WeatherIcon(private val iconName: String) {
    // Klart vær (01)
    CLEAR_DAY("01d.svg"),
    CLEAR_MORNING("01m.svg"),
    CLEAR_NIGHT("01n.svg"),
    
    // Delvis skyet (02-03)
    PARTLY_CLOUDY_DAY("02d.svg"),
    PARTLY_CLOUDY_MORNING("02m.svg"),
    PARTLY_CLOUDY_NIGHT("02n.svg"),
    CLOUDY_SUN_DAY("03d.svg"),
    CLOUDY_SUN_MORNING("03m.svg"),
    CLOUDY_SUN_NIGHT("03n.svg"),
    
    // Overskyet (04)
    CLOUDY("04.svg"),
    
    // Regn (05)
    RAIN_DAY("05d.svg"),
    RAIN_MORNING("05m.svg"),
    RAIN_NIGHT("05n.svg"),
    
    // Regn og torden (06)
    RAIN_THUNDER_DAY("06d.svg"),
    RAIN_THUNDER_MORNING("06m.svg"),
    RAIN_THUNDER_NIGHT("06n.svg"),
    
    // Sludd/Snø (07-08)
    SLEET_DAY("07d.svg"),
    SLEET_MORNING("07m.svg"),
    SLEET_NIGHT("07n.svg"),
    SNOW_SUN_DAY("08d.svg"),
    SNOW_SUN_MORNING("08m.svg"),
    SNOW_SUN_NIGHT("08n.svg"),
    
    // Regn (09-11)
    RAIN("09.svg"),
    HEAVY_RAIN_SHOWERS("10.svg"),
    HEAVY_RAIN("11.svg"),
    
    // Sludd og snø (12-15)
    SLEET_THUNDER("12.svg"),
    SNOW("13.svg"),
    SNOW_THUNDER("14.svg"),
    FOG("15.svg"),
    
    // Regn og torden med sludd/snø (20-22)
    SLEET_THUNDER_DAY("20d.svg"),
    SLEET_THUNDER_MORNING("20m.svg"),
    SLEET_THUNDER_NIGHT("20n.svg"),
    SNOW_THUNDER_DAY("21d.svg"),
    SNOW_THUNDER_MORNING("21m.svg"),
    SNOW_THUNDER_NIGHT("21n.svg"),
    RAIN_THUNDER("22.svg"),
    
    // Regn og torden (23-25)
    SLEET_SHOWERS_DAY("23.svg"),
    THUNDER_DAY("24d.svg"),
    THUNDER_MORNING("24m.svg"),
    THUNDER_NIGHT("24n.svg"),
    SLEET_SHOWERS("25d.svg"),
    
    // Snø og sludd (26-29)
    SNOW_SHOWERS_DAY("26.svg"),
    SLEET_SHOWERS_THUNDER_DAY("27d.svg"),
    SLEET_SHOWERS_THUNDER_MORNING("27m.svg"),
    SLEET_SHOWERS_THUNDER_NIGHT("27n.svg"),
    SNOW_SHOWERS_THUNDER_DAY("28d.svg"),
    SNOW_SHOWERS_THUNDER_MORNING("28m.svg"),
    SNOW_SHOWERS_THUNDER_NIGHT("28n.svg"),
    SNOW_SHOWERS_THUNDER_DAY_HEAVY("29d.svg"),
    SNOW_SHOWERS_THUNDER_MORNING_HEAVY("29m.svg"),
    SNOW_SHOWERS_THUNDER_NIGHT_HEAVY("29n.svg"),
    
    // Regn og torden (30-34)
    RAIN_THUNDER_LIGHT("30.svg"),
    RAIN_THUNDER_HEAVY("31.svg"),
    SLEET_AND_THUNDER("32.svg"),
    SNOW_AND_THUNDER("33.svg"),
    SNOW_AND_THUNDER_HEAVY("34.svg"),
    
    // Regn og snø (40-50)
    DRIZZLE_DAY("40d.svg"),
    DRIZZLE_MORNING("40m.svg"),
    DRIZZLE_NIGHT("40n.svg"),
    RAIN_SHOWERS_DAY("41d.svg"),
    RAIN_SHOWERS_MORNING("41m.svg"),
    RAIN_SHOWERS_NIGHT("41n.svg"),
    SLEET_SHOWERS_DAY_LIGHT("42d.svg"),
    SLEET_SHOWERS_MORNING_LIGHT("42m.svg"),
    SLEET_SHOWERS_NIGHT_LIGHT("42n.svg"),
    SNOW_SHOWERS_DAY_HEAVY("43d.svg"),
    SNOW_SHOWERS_MORNING_HEAVY("43m.svg"),
    SNOW_SHOWERS_NIGHT_HEAVY("43n.svg"),
    SNOW_SHOWERS_DAY_LIGHT("44d.svg"),
    SNOW_SHOWERS_MORNING_LIGHT("44m.svg"),
    SNOW_SHOWERS_NIGHT_LIGHT("44n.svg"),
    SNOW_SHOWERS_DAY_HEAVY_2("45d.svg"),
    SNOW_SHOWERS_MORNING_HEAVY_2("45m.svg"),
    SNOW_SHOWERS_NIGHT_HEAVY_2("45n.svg"),
    RAIN_SHOWERS_LIGHT("46.svg"),
    SLEET_LIGHT("47.svg"),
    SNOW_HEAVY("48.svg"),
    SNOW_LIGHT("49.svg"),
    SNOW_HEAVY_2("50.svg");

    // Henter full sti til SVG filen i assets
    fun getAssetPath(): String = "symbols/lightmode/svg/$iconName"

    companion object {
        fun fromWeatherCode(code: String, hour: Int): WeatherIcon {
            // Bestem tid på døgnet
            val timeOfDay = when {
                hour in 6..9 -> "m"  // Morgen
                hour in 10..17 -> "d" // Dag
                hour in 18..20 -> "m" // Kveld
                else -> "n" // Natt
            }

            // Map weather code to icon
            return when (code) {
                "clearsky" -> when (timeOfDay) {
                    "d" -> CLEAR_DAY
                    "m" -> CLEAR_MORNING
                    else -> CLEAR_NIGHT
                }
                "cloudy" -> CLOUDY
                "fair" -> when (timeOfDay) {
                    "d" -> PARTLY_CLOUDY_DAY
                    "m" -> PARTLY_CLOUDY_MORNING
                    else -> PARTLY_CLOUDY_NIGHT
                }
                "fog" -> FOG
                "heavyrain" -> HEAVY_RAIN
                "heavyrainandthunder" -> RAIN_THUNDER
                "heavyrainshowers" -> HEAVY_RAIN_SHOWERS
                "heavyrainshowersandthunder" -> RAIN_THUNDER
                "heavysleet" -> SLEET_DAY
                "heavysleetandthunder" -> SLEET_THUNDER_DAY
                "heavysleetshowers" -> SLEET_SHOWERS_DAY
                "heavysleetshowersandthunder" -> SLEET_SHOWERS_THUNDER_DAY
                "heavysnow" -> SNOW
                "heavysnowandthunder" -> SNOW_THUNDER_DAY
                "heavysnowshowers" -> SNOW_SHOWERS_DAY
                "heavysnowshowersandthunder" -> SNOW_SHOWERS_THUNDER_DAY
                "lightrain" -> RAIN_DAY
                "lightrainandthunder" -> RAIN_THUNDER_DAY
                "lightrainshowers" -> RAIN_SHOWERS_DAY
                "lightrainshowersandthunder" -> RAIN_THUNDER_DAY
                "lightsleet" -> SLEET_DAY
                "lightsleetandthunder" -> SLEET_THUNDER_DAY
                "lightsleetshowers" -> SLEET_SHOWERS_DAY
                "lightsnow" -> SNOW_SUN_DAY
                "lightsnowandthunder" -> SNOW_THUNDER_DAY
                "lightsnowshowers" -> SNOW_SHOWERS_DAY
                "lightssleetshowersandthunder" -> SLEET_SHOWERS_THUNDER_DAY
                "lightssnowshowersandthunder" -> SNOW_SHOWERS_THUNDER_DAY
                "partlycloudy" -> when (timeOfDay) {
                    "d" -> PARTLY_CLOUDY_DAY
                    "m" -> PARTLY_CLOUDY_MORNING
                    else -> PARTLY_CLOUDY_NIGHT
                }
                "rain" -> RAIN
                "rainandthunder" -> RAIN_THUNDER
                "rainshowers" -> RAIN_SHOWERS_DAY
                "rainshowersandthunder" -> RAIN_THUNDER_DAY
                "sleet" -> SLEET_DAY
                "sleetandthunder" -> SLEET_THUNDER_DAY
                "sleetshowers" -> SLEET_SHOWERS_DAY
                "sleetshowersandthunder" -> SLEET_SHOWERS_THUNDER_DAY
                "snow" -> SNOW
                "snowandthunder" -> SNOW_THUNDER_DAY
                "snowshowers" -> SNOW_SHOWERS_DAY
                "snowshowersandthunder" -> SNOW_SHOWERS_THUNDER_DAY
                else -> CLEAR_DAY // Default to clear sky if code not recognized
            }
        }
    }
}

// Data class for å holde vær og temperatur sammen
data class WeatherInfo(
    val temperature: Double,
    val weatherIcon: WeatherIcon,
    val description: String
) 