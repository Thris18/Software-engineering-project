package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.core.utils

import android.content.Context
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer

object MapLibreInitializer {
    fun initialize(context: Context) {
        MapLibre.getInstance(context, "oMZQoq4zniKOHeMvi7oA", WellKnownTileServer.MapTiler)
    }
}