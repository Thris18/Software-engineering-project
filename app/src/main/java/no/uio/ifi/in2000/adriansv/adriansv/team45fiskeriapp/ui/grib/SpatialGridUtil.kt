package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class GridCell(val x: Int, val y: Int)

class SpatialGrid(private val minDistanceKm: Double) {
    private val grid = mutableMapOf<GridCell, MutableList<Pair<Double, Double>>>()
    private val cellSize = minDistanceKm / 111.0 // ca. 1 grad = 111 km

    private fun getCell(lat: Double, lon: Double): GridCell {
        val x = (lon / cellSize).toInt()
        val y = (lat / cellSize).toInt()
        return GridCell(x, y)
    }

    fun isFarFromAll(lat: Double, lon: Double): Boolean {
        val cell = getCell(lat, lon)
        for (dx in -1..1) for (dy in -1..1) {
            val neighbor = GridCell(cell.x + dx, cell.y + dy)
            grid[neighbor]?.forEach { (otherLat, otherLon) ->
                if (haversine(lat, lon, otherLat, otherLon) < minDistanceKm) return false
            }
        }
        return true
    }

    fun addPoint(lat: Double, lon: Double) {
        val cell = getCell(lat, lon)
        grid.getOrPut(cell) { mutableListOf() }.add(lat to lon)
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
} 