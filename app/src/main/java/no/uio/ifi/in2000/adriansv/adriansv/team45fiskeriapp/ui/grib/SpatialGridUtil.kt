package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.grib

data class GridCell(val x: Int, val y: Int)

class SpatialGrid(val minDistanceKm: Double) {
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
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }
} 