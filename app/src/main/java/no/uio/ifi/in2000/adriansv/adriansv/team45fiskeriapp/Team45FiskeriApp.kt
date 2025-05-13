package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

// Kun for at ZonedDateTime skal fungere for alle, også på Android < 26
class Team45FiskeriApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
    }
}