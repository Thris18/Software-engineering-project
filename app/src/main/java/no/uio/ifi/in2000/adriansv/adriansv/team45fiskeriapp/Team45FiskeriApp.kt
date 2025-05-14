package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

// So that ZonedDateTime can work on Android 26 <
class Team45FiskeriApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
    }
}