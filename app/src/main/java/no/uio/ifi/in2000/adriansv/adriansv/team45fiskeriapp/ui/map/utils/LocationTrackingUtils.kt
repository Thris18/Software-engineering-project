package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.map.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.sources.GeoJsonSource

private const val TAG = "LocationTrackingUtils"

object LocationTrackingUtils {
    fun setupLocationTracking(
        context: Context,
        mapLibreMap: MapLibreMap?,
        userLocationSource: GeoJsonSource,
        routeSource: GeoJsonSource,
        isTrackingUser: Boolean,
        isFishingTripActive: Boolean,
        fishingTripRoute: List<LatLng>,
        onLocationUpdate: (LatLng) -> Unit,
        onRouteUpdate: (List<LatLng>) -> Unit
    ) {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L, // Update every second
                    1f // Update if user moves more than 1 meter
                ) { location ->
                    
                    // Update user location
                    val newLocation = LatLng(location.latitude, location.longitude)
                    onLocationUpdate(newLocation)
                    
                    // Update GeoJSON source with user location
                    updateUserLocationOnMap(userLocationSource, location)
                    
                    // If fishing trip is active, add position to route
                    if (isFishingTripActive) {
                        updateFishingTripRoute(
                            location = newLocation,
                            currentRoute = fishingTripRoute,
                            routeSource = routeSource,
                            onRouteUpdate = onRouteUpdate
                        )
                    }

                    // If tracking user, update camera
                    if (isTrackingUser) {
                        mapLibreMap?.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(newLocation, 18.0)
                        )
                    }
                }
            } else {
                Log.e(TAG, "GPS provider is not enabled")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up location tracking: ${e.message}")
        }
    }

    private fun updateUserLocationOnMap(source: GeoJsonSource, location: Location) {
        val geoJson = """
        {
            "type": "Feature",
            "geometry": {
                "type": "Point",
                "coordinates": [${location.longitude}, ${location.latitude}]
            }
        }
        """
        try {
            source.setGeoJson(geoJson)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user location on map: ${e.message}")
        }
    }

    private fun updateFishingTripRoute(
        location: LatLng,
        currentRoute: List<LatLng>,
        routeSource: GeoJsonSource,
        onRouteUpdate: (List<LatLng>) -> Unit
    ) {
        // Check if new position is different from last in route
        val lastPosition = currentRoute.lastOrNull()
        
        if (lastPosition == null ||
            (lastPosition.latitude != location.latitude ||
             lastPosition.longitude != location.longitude)) {
            
            val newRoute = currentRoute + location
            onRouteUpdate(newRoute)

            // Update route on map
            val routeGeoJson = """
            {
                "type": "Feature",
                "geometry": {
                    "type": "LineString",
                    "coordinates": [${newRoute.joinToString(",") { "[${it.longitude}, ${it.latitude}]" }}]
                }
            }
            """
            try {
                routeSource.setGeoJson(routeGeoJson)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating route on map: ${e.message}")
            }
        }
    }
} 