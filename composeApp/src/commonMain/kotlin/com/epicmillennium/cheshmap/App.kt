package com.epicmillennium.cheshmap

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import co.touchlab.kermit.Logger
import com.epicmillennium.cheshmap.maps.CameraPosition
import com.epicmillennium.cheshmap.maps.GoogleMaps
import com.epicmillennium.cheshmap.maps.LatLong
import com.epicmillennium.cheshmap.maps.Location
import com.epicmillennium.cheshmap.maps.isLocationValid
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {

    var isFirstUpdate by remember { mutableStateOf(true) } // force map to update at least once

    var userLocation = remember { Location(0.0, 0.0) }
    var shouldSetInitialCameraPosition = remember { CameraPosition() }

    // Map Commands
    var shouldCenterCameraOnLocation by remember {
        mutableStateOf<Location?>(null) // used to center map on user location
    }

    // 1) Update user GPS location - will be moved from here
    LaunchedEffect(Unit) {
        userLocation = Location(latitude = 43.1421722, longitude = 24.7320678)
//        while (true) {
//            val commonGpsLocationService = GPSService()
//            commonGpsLocationService.onUpdatedGPSLocation(
//                errorCallback = { errorMessage ->
//                    Log.e(this.toString(), "Error: $errorMessage")
//                },
//                locationCallback = { updatedLocation ->
//                    updatedLocation?.let { location ->
//                        userLocation = location
//                    } ?: run {
//                        Log.e(this.toString(), "Unable to get current location - 1")
//                    }
//                })
//            delay(2.seconds)
//        }
    }

    MaterialTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { content ->
            Box(
                modifier = Modifier.fillMaxSize().padding(content),
                contentAlignment = Alignment.Center
            ) {
                GoogleMaps(
                    userLocation,
                    isMapOptionSwitchesVisible = true,
                    shouldCenterCameraOnLatLong = remember(shouldCenterCameraOnLocation) {
                        if(!isFirstUpdate) {
                            Logger.d("💿 MapContent.shouldCenterCameraOnLatLong: ➤➤➤ Centering camera position")
                            shouldCenterCameraOnLocation?.let {
                                LatLong(
                                    shouldCenterCameraOnLocation!!.latitude,
                                    shouldCenterCameraOnLocation!!.longitude
                                )
                            } ?: run {
                                null
                            }
                        } else {
                            null
                        }
                    },
                    shouldSetInitialCameraPosition = if (userLocation.isLocationValid()) {
                        CameraPosition(
                            target = LatLong(
                                userLocation.latitude,
                                userLocation.longitude
                            ),
                            zoom = 14f  // note: forced zoom level
                        )
                    } else {
                        null
                    },
                    onFindMeButtonClick = {
                        shouldCenterCameraOnLocation = userLocation.copy()
                    },
                )
            }

            // Indicate first update has occurred
            isFirstUpdate = false
        }
    }
}