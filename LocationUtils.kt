package com.example.newcanalcollection.utils

import android.content.Context
import android.location.LocationManager
import com.example.newcanalcollection.R
import com.example.newcanalcollection.managers.PermissionManager

class LocationUtils {

    companion object {
        fun handleLocationRequest(
            permissionManager: PermissionManager,
            successAction: () -> Unit,
            notEnabledAction: () -> Unit,
            notGrantedAction: () -> Unit
        ) {
            // Check if permission is granted or not
            permissionManager
                .request(Permission.Location)
                .rationale(R.string.location_permission_request)
                .checkPermission { granted ->

                    if (granted) {
                        // Permission granted
                        // check if gps is enabled
                        if (isLocationEnabled(
                                permissionManager.getContext()
                            )
                        ) {
                            // do what you want
                            successAction()

                        } else {
                            // Not enabled
                            notEnabledAction()
                        }

                    } else {
                        // Not granted
                        notGrantedAction()
                    }
                }
        }

        // Check if location location enabled
        private fun isLocationEnabled(context: Context): Boolean {
            val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        }
    }
}