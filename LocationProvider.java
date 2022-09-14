package com.eps.highusers.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.concurrent.TimeUnit;

public class LocationProvider {

    private static LocationProvider mInstance;

    private final LocationManager locationManager;
    private final FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;


    public static Long UPDATE_INTERVAL_SECS = 10L;
    public static Long FASTEST_UPDATE_INTERVAL_SECS = 5L;
    public static Long MAXIMUM_WAIT_TIME_SECS = 15L;
    public static Float SMALLEST_DISPLACEMENT_METER = 1F;

    private LocationListener networkLocationListener;
    private LocationListener gpsLocationListener;
    private LocationCallback fusedLocationCallback;

    private final LocationObserver locationObserver;

    public static LocationProvider getInstance(Context context, LocationObserver locationObserver) {
        if (mInstance == null)
            mInstance = new LocationProvider(context, locationObserver);

        return mInstance;
    }

    private LocationProvider(Context context, LocationObserver locationObserver) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        this.locationObserver = locationObserver;
        buildLocationRequest();
        buildLocationCallBacks();
    }

    @SuppressLint("MissingPermission")
    public void startLocationUpdates(Context context) {
        if (isPermissionGranted(context)) {

            // 1- Network Provider
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetworkEnabled) {
                Log.d("LocationTest" , "Network Provider is working");
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        TimeUnit.SECONDS.toMillis(UPDATE_INTERVAL_SECS),
                        SMALLEST_DISPLACEMENT_METER,
                        networkLocationListener);
            }

            // 2- GPS Provider
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled) {
                Log.d("LocationTest" , "GPS Provider is working");
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        TimeUnit.SECONDS.toMillis(UPDATE_INTERVAL_SECS),
                        SMALLEST_DISPLACEMENT_METER,
                        gpsLocationListener);
            }

            // 3- Fused Provider
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    fusedLocationCallback,
                    Looper.getMainLooper());
        } else
            Toast.makeText(context, "من فضلك قم بمنح إذن الوصول للموقع الجغرافي", Toast.LENGTH_LONG).show();
    }

    public void stopLocationUpdates() {
        locationManager.removeUpdates(networkLocationListener);
        locationManager.removeUpdates(gpsLocationListener);
        fusedLocationClient.removeLocationUpdates(fusedLocationCallback);
    }

    // Build a location request
    private void buildLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(TimeUnit.SECONDS.toMillis(UPDATE_INTERVAL_SECS));
        locationRequest.setFastestInterval(TimeUnit.SECONDS.toMillis(FASTEST_UPDATE_INTERVAL_SECS));
        locationRequest.setMaxWaitTime(TimeUnit.SECONDS.toMillis(MAXIMUM_WAIT_TIME_SECS));
        locationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT_METER);
    }

    // Build the location callback object and obtain the location results //as demonstrated below:
    private void buildLocationCallBacks() {
        networkLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                    Log.d("LocationTest" , " .. Location is : " + location);
                    locationObserver.onGettingLocationUpdates(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                LocationListener.super.onStatusChanged(provider, status, extras);
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
                LocationListener.super.onProviderEnabled(provider);
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                LocationListener.super.onProviderDisabled(provider);
            }
        };

        gpsLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                Log.d("LocationTest" , " .. Location is : " + location);
                locationObserver.onGettingLocationUpdates(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                LocationListener.super.onStatusChanged(provider, status, extras);
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
                LocationListener.super.onProviderEnabled(provider);
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                LocationListener.super.onProviderDisabled(provider);
            }
        };

        fusedLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    Log.d("LocationTest" , " Location is : " + location);
                    locationObserver.onGettingLocationUpdates(location);
                } 
            }
        };
    }

    private Boolean isPermissionGranted(Context context) {
        return (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED);
    }

    public interface LocationObserver {
        void onGettingLocationUpdates(Location location);
    }
}
