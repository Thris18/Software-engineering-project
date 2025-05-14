package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather

enum class WeatherIcon(private val iconName: String) {
    // Klart vær (01)
    CLEAR_DAY("01d.svg"),
    CLEAR_MORNING("01m.svg"),
    CLEAR_NIGHT("01n.svg"),

    // Delvis skyet (02-03)
    PARTLY_CLOUDY_DAY("02d.svg"),
    PARTLY_CLOUDY_MORNING("02m.svg"),
    PARTLY_CLOUDY_NIGHT("02n.svg"),

    // Overskyet (04)
    CLOUDY("04.svg"),

    // Regn (05)
    RAIN_DAY("05d.svg"),

    // Regn og torden (06)
    RAIN_THUNDER_DAY("06d.svg"),

    // Sludd/Snø (07-08)
    SLEET_DAY("07d.svg"),
    SNOW_SUN_DAY("08d.svg"),

    // Regn (09-11)
    RAIN("09.svg"),
    HEAVY_RAIN_SHOWERS("10.svg"),
    HEAVY_RAIN("11.svg"),

    // Sludd og snø (12-15)
    SNOW("13.svg"),
    FOG("15.svg"),

    // Regn og torden med sludd/snø (20-22)
    SLEET_THUNDER_DAY("20d.svg"),
    SNOW_THUNDER_DAY("21d.svg"),
    RAIN_THUNDER("22.svg"),

    // Regn og torden (23-25)
    SLEET_SHOWERS_DAY("23.svg"),

    // Snø og sludd (26-29)
    SNOW_SHOWERS_DAY("26.svg"),
    SLEET_SHOWERS_THUNDER_DAY("27d.svg"),
    SNOW_SHOWERS_THUNDER_DAY("28d.svg"),

    // Regn og snø (40-50)

    RAIN_SHOWERS_DAY("41d.svg");


    // Henter full sti til SVG filen i assets
    fun getAssetPath(): String = "symbols/lightmode/svg/$iconName"

    companion object {
        fun fromWeatherCode(code: String, hour: Int): WeatherIcon {
            // Bestem tid på døgnet
            val timeOfDay = when (hour) {
                in 6..9 -> "m"  // Morgen
                in 10..17 -> "d" // Dag
                in 18..20 -> "m" // Kveld
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
                else -> CLEAR_DAY
            }
        }
    }
}