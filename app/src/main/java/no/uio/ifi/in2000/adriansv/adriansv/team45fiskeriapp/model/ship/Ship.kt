package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.ship

data class Ship(
    val mmsi: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val messageTime: String,
    val type: String,
    val displayType: String,
    val speed: Double = 0.0,
    val course: Double = 0.0
)

data class ShipResponse(
    val ships: List<Ship>
)

data class AccessToken(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Int
) 