package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.ship

data class Ship(
    val mmsi: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val messageTime: String,
    val type: String,
    val displayType: String,  // Norsk navn på skipstypen
    val speed: Double = 0.0,
    val course: Double = 0.0
)

data class ShipResponse(
    val ships: List<Ship>
)

//For håndtering av access token
data class AccessToken(
    val access_token: String,
    val token_type: String,
    val expires_in: Int
) 