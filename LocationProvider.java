package com.eps.highusers.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.concurrent.TimeUnit;

public class LocationProvider {

    private static LocationProvider mInstance;
    private final FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private Double lat;
    private Double lang;

    public static LocationProvider getInstance(Context context){
        if (mInstance == null)
            mInstance = new LocationProvider(context);

        return mInstance;
    }

    private LocationProvider(Context context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        buildLocationRequest();
        buildLocationCallBack();
    }

    @SuppressLint("MissingPermission")
    public void startLocationUpdates(Context context) {
        if (isPermissionGranted(context)) {
            fusedLocationClient.requestLocationUpdates
                    (
                            locationRequest,
                            locationCallback,
                            Looper.getMainLooper());
        } else
            Toast.makeText(context, "من فضلك قم بمنح إذن الوصول للموقع الجغرافي", Toast.LENGTH_LONG).show();
    }

    public void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    public Double getLatitude() {
        return lat != null ? lat : 0.0;
    }

    public Double getLongitude() {
        return lang != null ? lang : 0.0;
    }

    // Build a location request
    private void buildLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(TimeUnit.SECONDS.toMillis(60));
        locationRequest.setFastestInterval(TimeUnit.SECONDS.toMillis(30));
        locationRequest.setMaxWaitTime(TimeUnit.SECONDS.toMillis(120));
        locationRequest.setSmallestDisplacement(100);
    }

    // Build the location callback object and obtain the location results //as demonstrated below:
    private void buildLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location locationRes : locationResult.getLocations()) {
                    // Update UI with location data
                    lat = locationRes.getLatitude();
                    lang = locationRes.getLongitude();
                }
            }
        };
    }

    private Boolean isPermissionGranted(Context context) {
        return (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);

    }
}

