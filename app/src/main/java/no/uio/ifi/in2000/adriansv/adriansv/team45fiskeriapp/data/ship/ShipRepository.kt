package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.ship

import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.ship.ShipResponse

interface ShipRepository {
    suspend fun fetchShips(): Result<ShipResponse>

    class ShipRepositoryImpl(
        private val dataSource: ShipDataSource
    ) : ShipRepository {
        constructor() : this(ShipDataSource())

        override suspend fun fetchShips(): Result<ShipResponse> {
            return dataSource.fetchShips()
        }
    }
}
