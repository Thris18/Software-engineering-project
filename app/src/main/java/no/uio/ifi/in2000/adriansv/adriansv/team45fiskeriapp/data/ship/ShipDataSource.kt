package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.data.ship

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.ship.AccessToken
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.ship.Ship
import no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.ship.ShipResponse
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * SKIPSDATAKILDE FOR FISKERIAPPLIKASJONEN
 * 
 * Denne filen implementerer datakilde-laget for å hente sanntids skipsdata fra 
 * BarentsWatch AIS-API. Den håndterer autentisering, datafetching og parsing.
 * 
 * NB: Koden inneholder direkte API-nøkler og hemmeligheter, noe som ikke er 
 * best praksis i produksjon - disse bør ideelt sett lagres sikkert.
 * 
 * Nøkkelfunksjonalitet:
 * - OAuth2-autentisering mot BarentsWatch for å få tilgangstoken
 * - Henting av sanntidsdata om skip fra AIS-API
 * - Konvertering av rå JSON-data til strukturerte Ship-objekter
 * - Klassifisering av skip basert på skipstypekoder
 * - Filtrering av ugyldige skipsposter (f.eks. med 0-koordinater)
 * 
 * Filen inneholder:
 * - ShipDataSource: Hovedklassen som henter skipsdata
 * - getAccessToken: Privat metode for å hente OAuth2-token
 * - fetchShips: Hovedmetoden for å hente skipsinformasjon
 * - isValidShip: Hjelpemetode for å validere skipsposter
 * 
 * Dataene fra denne kilden brukes til å vise fartøyposisjoner på kartet,
 * noe som er viktig for sikkerheten og informasjonsverdien i appen.
 */

private const val TAG = "ShipDataSource"
private const val TOKEN_URL = "https://id.barentswatch.no/connect/token"
private const val AIS_URL = "https://live.ais.barentswatch.no/v1/latest/combined"

class ShipDataSource {
    private suspend fun getAccessToken(): AccessToken? {
        return try {
            val clientId = "aayanali492@gmail.com:aayanklient"
            val clientSecret = "hemmeligheten"

            val connection = withContext(Dispatchers.IO) {
                URL(TOKEN_URL).openConnection()
            } as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

            val postData = withContext(Dispatchers.IO) {
                URLEncoder.encode("client_id", "UTF-8")
            } + "=" + withContext(Dispatchers.IO) {
                URLEncoder.encode(clientId, "UTF-8")
            } +
                    "&" + withContext(Dispatchers.IO) {
                URLEncoder.encode("scope", "UTF-8")
            } + "=" + withContext(Dispatchers.IO) {
                URLEncoder.encode("ais", "UTF-8")
            } +
                    "&" + withContext(Dispatchers.IO) {
                URLEncoder.encode("client_secret", "UTF-8")
            } + "=" + withContext(Dispatchers.IO) {
                URLEncoder.encode(clientSecret, "UTF-8")
            } +
                    "&" + withContext(Dispatchers.IO) {
                URLEncoder.encode("grant_type", "UTF-8")
            } + "=" + withContext(Dispatchers.IO) {
                URLEncoder.encode("client_credentials", "UTF-8")
            }

            connection.outputStream.use { it.write(postData.toByteArray()) }

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                val jsonObject = JSONObject(response)
                AccessToken(
                    accessToken = jsonObject.getString("access_token"),
                    tokenType = jsonObject.getString("token_type"),
                    expiresIn = jsonObject.getInt("expires_in")
                )
            } else {
                val errorResponse = connection.errorStream?.bufferedReader()?.readText()
                Log.e(TAG, "Failed to get access token. Response code: ${connection.responseCode}, Error: $errorResponse")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting access token", e)
            null
        }
    }
    
    suspend fun fetchShips(): Result<ShipResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = getAccessToken() ?: return@withContext Result.failure(
                    Exception("Failed to get access token")
                )

                val connection = URL(AIS_URL).openConnection() as HttpURLConnection
                connection.setRequestProperty("Authorization", "Bearer ${token.accessToken}")
                connection.setRequestProperty("Accept", "application/json")

                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val jsonArray = JSONArray(response)
                    val ships = mutableListOf<Ship>()

                    for (i in 0 until jsonArray.length()) {
                        val shipJson = jsonArray.getJSONObject(i)
                        val mmsi = shipJson.getString("mmsi")

                        val name = if (shipJson.has("name") && !shipJson.isNull("name") && shipJson.getString("name") != "string") 
                            shipJson.getString("name") 
                        else 
                            "Ukjent"
                            
                        // Get type of ship
                        val type = when (shipJson.optInt("shipType", 0)) {

                            20, 21, 22, 23, 24, 25 -> "annen_fartoytype" // WIG

                            30 -> "fiskefartoy"

                            31, 32, 52 -> "taubat"  // Towing/Pushing/Tug
                            33 -> "annen_fartoytype"  // Dredger
                            34 -> "dykkerfartoy"  // Dive vessel
                            35 -> "reservert"  // Military

                            36, 37 -> "mindre_arbeidsbat"  // Pleasure/Sailing

                            40, 41, 42, 43, 44, 45, 46, 47, 48, 49 -> "hoyhastighetsfartoy"

                            50 -> "losbat"  // Pilot
                            51 -> "sar"  // Search and Rescue
                            54 -> "annen_fartoytype"  // Anti pollution
                            55 -> "politi"  // Law enforcement

                            60, 61, 62, 63, 64, 65, 66, 67, 68, 69 -> "passasjerfartoy"

                            70, 71, 72, 73, 74, 75, 76, 77, 78, 79 -> "fraktefartoy"

                            80, 81, 82, 83, 84, 85, 86, 87, 88, 89 -> "fraktefartoy"

                            else -> "annen_fartoytype"
                        }

                        val displayType = when (shipJson.optInt("shipType", 0)) {
                            20, 21, 22, 23, 24, 25 -> "WIG-fartøy"
                            30 -> "Fiskefartøy"
                            31, 32, 52 -> "Slepebåt"
                            33 -> "Mudringsfartøy"
                            34 -> "Dykkerfartøy"
                            35 -> "Militært fartøy"
                            36, 37 -> "Fritidsbåt"
                            40, 41, 42, 43, 44, 45, 46, 47, 48, 49 -> "Høyhastighetsfartøy"
                            50 -> "Losbåt"
                            51 -> "SAR-fartøy"
                            54 -> "Forurensningskontroll"
                            55 -> "Politi/Kystvakt"
                            60, 61, 62, 63, 64, 65, 66, 67, 68, 69 -> "Passasjerfartøy"
                            70, 71, 72, 73, 74, 75, 76, 77, 78, 79 -> "Lasteskip"
                            80, 81, 82, 83, 84, 85, 86, 87, 88, 89 -> "Tankskip"
                            else -> "Annet fartøy"
                        }
                        
                        val messageTime = shipJson.optString("msgtime", "")
                        val latitude = shipJson.optDouble("latitude", 0.0)
                        val longitude = shipJson.optDouble("longitude", 0.0)
                        val speed = shipJson.optDouble("speedOverGround", 0.0)
                        val course = shipJson.optDouble("courseOverGround", 0.0)
                        
                        if (isValidShip(latitude, longitude, speed)) {
                            val ship = Ship(
                                mmsi = mmsi,
                                name = name,
                                type = type,
                                displayType = displayType,
                                messageTime = messageTime,
                                latitude = latitude,
                                longitude = longitude,
                                speed = speed,
                                course = course
                            )
                            ships.add(ship)
                        }
                    }



                    Result.success(ShipResponse(ships))
                } else {
                    val errorMessage = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                    Log.e(TAG, "Failed to fetch ships. Response code: ${connection.responseCode}, Error: $errorMessage")
                    Result.failure(Exception("Failed to fetch ships: ${connection.responseCode}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching ships", e)
                Result.failure(e)
            }
        }
    }
    
    private fun isValidShip(latitude: Double, longitude: Double, speed: Double): Boolean {
        return latitude != 0.0 && longitude != 0.0 && speed >= 0.0
    }
} 