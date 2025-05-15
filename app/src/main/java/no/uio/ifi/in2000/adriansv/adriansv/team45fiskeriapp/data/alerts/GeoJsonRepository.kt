package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.alerts

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface GeoJsonRepository {
    suspend fun fetchGeoJson(): Result<String>

    class GeoJsonRepositoryImpl(
        private val dataSource: GeoJsonDataSource
    ) : GeoJsonRepository {
        constructor() : this(GeoJsonDataSource())

        override suspend fun fetchGeoJson(): Result<String> = withContext(Dispatchers.IO) {
            try {
                val result = dataSource.fetchGeoJson()
                if (result != null) {
                    Result.success(result)
                } else {
                    Result.failure(Exception("Ingen internett-tilgang"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}