package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.model.fish

import java.util.Date

data class FishLog(
    val id: Long = System.currentTimeMillis(),
    val fishType: String,
    val area: String,
    val description: String = "",
    val weight: Float? = null,
    val imageUri: String? = null,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Date = Date()
)