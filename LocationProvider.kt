package com.example.newcanalcollection.managers.location


import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject


/*
This Class is used to get Location
remember --> you need to handle 2 events (GPS is not enabled , GPS permission is not granted)
you will find that in LocationUtils Class
 */

class LocationProvider @Inject constructor(
    context: Context
) {

    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val fusedLocationManager = LocationServices.getFusedLocationProviderClient(context)

    private var location: Location? = null

    // For Network Location Provider
    private var networkLocationListener: LocationListener? = null

    // For Network Location Provider
    private var gpsLocationListener: LocationListener? = null

    // For Fused Location API
    private var fusedLocationCallBack: LocationCallback? = null

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(): Flow<Location> = callbackFlow {

        /*
        First : Initialize Callbacks
         */
        networkLocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                Log.d("locationTest", "networkLocationListener : $location")
                trySend(location).isSuccess
            }

            override fun onProviderEnabled(provider: String) {}

            override fun onProviderDisabled(provider: String) {}

            // Deprecated annotation is very important to prevent crash
            // Don't delete it
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }
        }


        gpsLocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                Log.d("locationTest", "gpsLocationListener : $location")
                trySend(location).isSuccess
            }

            override fun onProviderEnabled(provider: String) {}

            override fun onProviderDisabled(provider: String) {}

            // Deprecated annotation is very important to prevent crash
            // Don't delete it
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }
        }

        fusedLocationCallBack = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location = locationResult.lastLocation
                Log.d("locationTest", "fusedLocationCallBack : $location")
                if (location != null)
                    trySend(location).isSuccess
            }
        }

        /*
         Second : Get Updates from 3 sources
         */
        // 1- Network Location Provider
        val hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (hasNetwork) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                TimeUnit.SECONDS.toMillis(UPDATE_INTERVAL_SEC),
                SMALLEST_DISPLACEMENT_METER,
                networkLocationListener!!
            )
        }

        // 2- GPS Location Provider
        val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (hasGps) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                TimeUnit.SECONDS.toMillis(UPDATE_INTERVAL_SEC),
                SMALLEST_DISPLACEMENT_METER,
                gpsLocationListener!!
            )
        }

        // 3- Fused Location Provider
        fusedLocationManager.requestLocationUpdates(
            buildFusedLocationRequest(),
            fusedLocationCallBack as LocationCallback,
            Looper.getMainLooper()
        )

        /*
         Finally : close updated after coroutine scope closed
         */
        awaitClose {
            stopLocationUpdates()
        }
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(): Task<Location?> {
        return fusedLocationManager.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        )
    }

    fun stopLocationUpdates() {
        networkLocationListener?.let {
            locationManager.removeUpdates(it)
            networkLocationListener = null
        }

        gpsLocationListener?.let {
            locationManager.removeUpdates(it)
            gpsLocationListener = null
        }
        fusedLocationCallBack?.let {
            fusedLocationManager.removeLocationUpdates(it)
            fusedLocationCallBack = null
        }
    }

    // Build a location request
    private fun buildFusedLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            // Sets the desired interval for active location updates.
            // This interval is inexact.
            interval = TimeUnit.SECONDS.toMillis(UPDATE_INTERVAL_SEC)
            // Sets the fastest rate for active location updates.
            // This interval is exact, and the application will never
            // receive updates more frequently than this value
            fastestInterval = TimeUnit.SECONDS.toMillis(FASTEST_UPDATE_INTERVAL_SEC)
            // Sets the maximum time when batched location updates are delivered.
            // Updates may be delivered sooner than this interval
            maxWaitTime = TimeUnit.SECONDS.toMillis(MAXIMUM_WAIT_TIME_SEC)
            // Sets the minimum displacement between location updates in meters
            smallestDisplacement = SMALLEST_DISPLACEMENT_METER
            // Sets the priority of the request.
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }
    }

    companion object {
        private const val UPDATE_INTERVAL_SEC = 30L
        private const val FASTEST_UPDATE_INTERVAL_SEC = 10L
        private const val MAXIMUM_WAIT_TIME_SEC = 60L
        private const val SMALLEST_DISPLACEMENT_METER = 10F
    }
}