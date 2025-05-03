package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.weather

import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.weather.*
import org.json.JSONObject

class WeatherResponseParser {
    fun parseJsonToWeatherResponse(jsonObject: JSONObject): WeatherResponse {
        val type = jsonObject.getString("type")
        val geometry = parseGeometry(jsonObject.getJSONObject("geometry"))
        val properties = parseProperties(jsonObject.getJSONObject("properties"))
        return WeatherResponse(type = type, geometry = geometry, properties = properties)
    }
    
    private fun parseGeometry(geometryJson: JSONObject): Geometry {
        val type = geometryJson.getString("type")
        val coordinates = geometryJson.getJSONArray("coordinates").let { array ->
            List(array.length()) { array.getDouble(it) }
        }
        return Geometry(type = type, coordinates = coordinates)
    }
    
    private fun parseProperties(propertiesJson: JSONObject): Properties {
        val meta = parseMeta(propertiesJson.getJSONObject("meta"))
        val timeseries = parseTimeseries(propertiesJson.getJSONArray("timeseries"))
        return Properties(meta = meta, timeseries = timeseries)
    }
    
    private fun parseMeta(metaJson: JSONObject): Meta {
        val updated_at = metaJson.getString("updated_at")
        val units = parseUnits(metaJson.getJSONObject("units"))
        return Meta(updated_at = updated_at, units = units)
    }
    
    private fun parseUnits(unitsJson: JSONObject): Units {
        return Units(
            air_pressure_at_sea_level = unitsJson.getString("air_pressure_at_sea_level"),
            air_temperature = unitsJson.getString("air_temperature"),
            cloud_area_fraction = unitsJson.getString("cloud_area_fraction"),
            precipitation_amount = unitsJson.getString("precipitation_amount"),
            relative_humidity = unitsJson.getString("relative_humidity"),
            wind_from_direction = unitsJson.getString("wind_from_direction"),
            wind_speed = unitsJson.getString("wind_speed")
        )
    }
    
    private fun parseTimeseries(timeseriesArray: org.json.JSONArray): List<TimeSeriesEntry> {
        val timeseriesList = mutableListOf<TimeSeriesEntry>()
        
        for (i in 0 until timeseriesArray.length()) {
            val entry = timeseriesArray.getJSONObject(i)
            val time = entry.getString("time")
            val data = parseWeatherData(entry.getJSONObject("data"))
            timeseriesList.add(TimeSeriesEntry(time = time, data = data))
        }
        
        return timeseriesList
    }
    
    private fun parseWeatherData(dataJson: JSONObject): WeatherData {
        val instant = parseInstant(dataJson.getJSONObject("instant"))
        val next_1_hours = dataJson.optJSONObject("next_1_hours")?.let { parseNextHours(it) }
        val next_6_hours = dataJson.optJSONObject("next_6_hours")?.let { parseNextHours(it) }
        val next_12_hours = dataJson.optJSONObject("next_12_hours")?.let { parseNextHours(it) }
        
        return WeatherData(
            instant = instant,
            next_1_hours = next_1_hours,
            next_6_hours = next_6_hours,
            next_12_hours = next_12_hours
        )
    }
    
    private fun parseInstant(instantJson: JSONObject): Instant {
        val details = parseWeatherDetails(instantJson.getJSONObject("details"))
        return Instant(details = details)
    }
    
    private fun parseWeatherDetails(detailsJson: JSONObject): WeatherDetails {
        return WeatherDetails(
            air_pressure_at_sea_level = detailsJson.getDouble("air_pressure_at_sea_level"),
            air_temperature = detailsJson.getDouble("air_temperature"),
            cloud_area_fraction = detailsJson.getDouble("cloud_area_fraction"),
            relative_humidity = detailsJson.getDouble("relative_humidity"),
            wind_from_direction = detailsJson.getDouble("wind_from_direction"),
            wind_speed = detailsJson.getDouble("wind_speed")
        )
    }
    
    private fun parseNextHours(nextHoursJson: JSONObject): NextHours {
        val summary = parseWeatherSummary(nextHoursJson.getJSONObject("summary"))
        val details = parseNextHoursDetails(nextHoursJson.getJSONObject("details"))
        return NextHours(summary = summary, details = details)
    }
    
    private fun parseWeatherSummary(summaryJson: JSONObject): WeatherSummary {
        return WeatherSummary(symbol_code = summaryJson.getString("symbol_code"))
    }
    
    private fun parseNextHoursDetails(detailsJson: JSONObject): NextHoursDetails {
        return NextHoursDetails(
            precipitation_amount = detailsJson.optDouble("precipitation_amount", 0.0)
        )
    }
} 