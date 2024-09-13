package com.epicmillennium.cheshmap.maps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import cocoapods.GoogleMaps.GMSCameraPosition
import cocoapods.GoogleMaps.GMSCameraUpdate
import cocoapods.GoogleMaps.GMSCircle
import cocoapods.GoogleMaps.GMSMapStyle
import cocoapods.GoogleMaps.GMSMapView
import cocoapods.GoogleMaps.GMSMarker
import cocoapods.GoogleMaps.animateWithCameraUpdate
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.UIKit.UIColor

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun GoogleMaps(
    userLocation: Location,
    isMapOptionSwitchesVisible: Boolean,
    shouldCenterCameraOnLatLong: LatLong?,
    shouldSetInitialCameraPosition: CameraPosition?,
    onFindMeButtonClick: (() -> Unit)?,
) {

    var isMapSetupCompleted by remember { mutableStateOf(false) }
    var isMapRedrawTriggered by remember { mutableStateOf(true) }
    var didCameraPositionLatLongBoundsChange by remember { mutableStateOf(false) }
    var didCameraPositionChange by remember { mutableStateOf(false) }
    var didCameraLocationLatLongChange by remember { mutableStateOf(false) }

    val googleMapView = remember(isMapRedrawTriggered) {
        GMSMapView().apply {
            setMyLocationEnabled(true)
            settings.myLocationButton = true
            settings.scrollGestures = true
            settings.zoomGestures = true
            this.setMinZoom(6f, 17f)
            this.setMapStyle(
                GMSMapStyle.styleWithJSONString(
                    mapStyle1(),
                    error = null
                )
            )
        }
    }

    LaunchedEffect(shouldSetInitialCameraPosition) {
        if (shouldSetInitialCameraPosition != null) {
            didCameraPositionChange = true
        }
    }

    LaunchedEffect(shouldCenterCameraOnLatLong) {
        if (shouldCenterCameraOnLatLong != null) {
            didCameraLocationLatLongChange = true
        }
    }

    Box(Modifier.fillMaxSize()) {
        UIKitView(
            modifier = Modifier.fillMaxSize(),
            interactive = true,
            factory = {
                googleMapView.apply {
                    setDelegate(delegate)
                    this.setMyLocationEnabled(true)
                }

                googleMapView
            },
            onRelease = {
                it.removeFromSuperview()
            },
            update = { view ->
                if (true) { // TODO - should be isTrackingEnabled
                    userLocation?.let { myLocation ->
                        view.animateWithCameraUpdate(
                            GMSCameraUpdate.setTarget(
                                CLLocationCoordinate2DMake(
                                    latitude = myLocation.latitude,
                                    longitude = myLocation.longitude
                                ),
                                zoom = 14f
                            )
                        )
                    }
                } else {
                    if (!isMapSetupCompleted) { // Sets the camera once during setup, this allows the user to move the map around
                        shouldSetInitialCameraPosition?.let { cameraPosition ->
                            view.animateWithCameraUpdate(
                                GMSCameraUpdate.setTarget(
                                    CLLocationCoordinate2DMake(
                                        latitude = cameraPosition.target.latitude,
                                        longitude = cameraPosition.target.longitude,
                                    ),
                                    zoom = cameraPosition.zoom
                                )
                            )
                        }
                    }
                }

                // set the map up only once, this allows the user to move the map around
                if (!isMapSetupCompleted) {
                    view.settings.setAllGesturesEnabled(true)
                    view.settings.setScrollGestures(true)
                    view.settings.setZoomGestures(true)
                    view.settings.setCompassButton(false)

                    view.myLocationEnabled = true // show the users dot
                    view.settings.myLocationButton = false // we use our own location circle

                    isMapSetupCompleted = true
                }

                if (didCameraPositionChange) {
                    didCameraPositionChange = false
                    shouldSetInitialCameraPosition?.let { cameraPosition ->
                        view.setCamera(
                            GMSCameraPosition.cameraWithLatitude(
                                cameraPosition.target.latitude,
                                cameraPosition.target.longitude,
                                cameraPosition.zoom // Note Zoom level is forced here, which changes user's zoom level
                            )
                        )
                    }
                }

                if (didCameraLocationLatLongChange) {
                    didCameraLocationLatLongChange = false
                    shouldCenterCameraOnLatLong?.let { cameraLocation ->
                        view.animateWithCameraUpdate(
                            GMSCameraUpdate.setTarget(
                                CLLocationCoordinate2DMake(
                                    latitude = cameraLocation.latitude,
                                    longitude = cameraLocation.longitude
                                )
                            )
                        )
                    }
                }

                if (isMapRedrawTriggered) {
                    // reset the markers & polylines, selected marker, etc.
                    val oldSelectedMarker = view.selectedMarker
                    var curSelectedMarker: GMSMarker? = null
                    val curSelectedMarkerId = view.selectedMarker?.userData as? String
                    view.clear()

                    // render the user's location "talk" circle
                    userLocation?.let {
                        GMSCircle().apply {
                            position = CLLocationCoordinate2DMake(
                                userLocation.latitude,
                                userLocation.longitude
                            )
//                            radius = seenRadiusMiles.milesToMeters()
                            radius = 0.5
                            fillColor = UIColor.blueColor().colorWithAlphaComponent(0.4)
                            strokeColor = UIColor.whiteColor().colorWithAlphaComponent(0.8)
                            strokeWidth = 2.0
                            map = view
                        }
                    }

                    isMapRedrawTriggered = false
                }
            },
        )

        // Local Map Controls
        AnimatedVisibility(
            visible = isMapOptionSwitchesVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(16.dp)
                    .align(Alignment.BottomStart),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Bottom
            ) {
                Spacer(modifier = Modifier.weight(1f))
//                SwitchWithLabel(
//                    label = "Markers",
//                    state = isMarkersEnabled,
//                    darkOnLightTextColor = gmsMapViewType == kGMSTypeSatellite
//                ) {
//                    isMarkersEnabled = !isMarkersEnabled
//                    isMapRedrawTriggered = true
//                }
//                // LEAVE FOR FUTURE USE
//                //    SwitchWithLabel(
//                //        label = "Heat Map",
//                //        state = isHeatMapEnabled,
//                //        darkOnLightTextColor = true //smMapViewType == kGMSTypeSatellite
//                //    ) {
//                //        isHeatMapEnabled = !isHeatMapEnabled
//                //    }
//                SwitchWithLabel(
//                    label = "Satellite",
//                    state = gmsMapViewType == kGMSTypeSatellite,
//                    darkOnLightTextColor = gmsMapViewType == kGMSTypeSatellite
//                ) { shouldUseSatellite ->
//                    didMapTypeChange = true
//                    gmsMapViewType =
//                        if (shouldUseSatellite) kGMSTypeSatellite else kGMSTypeNormal
//                }
            }
        }

        Column(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd),
            horizontalAlignment = Alignment.End
        ) {
//            // Toggle tracking
//            if (onToggleIsTrackingEnabledClick != null) {
//                FloatingActionButton(
//                    modifier = Modifier
//                        .padding(16.dp),
//                    onClick = {
//                        onToggleIsTrackingEnabledClick()
//                    }) {
//                    Icon(
//                        imageVector = if (isTrackingEnabled)
//                            Icons.Default.Pause
//                        else Icons.Default.PlayArrow,
//                        contentDescription = "Toggle track your location"
//                    )
//                }
//            }

            // Center on user's
            if (onFindMeButtonClick != null) {
                FloatingActionButton(
                    modifier = Modifier
                        .padding(16.dp),
                    onClick = {
                        onFindMeButtonClick()
                    }) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Center on your location"
                    )
                }
            }
        }
    }
}

// https://mapstyle.withgoogle.com/
fun mapStyle1(): String {
    return """
    [
  {
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#242f3e"
      }
    ]
  },
  {
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#746855"
      }
    ]
  },
  {
    "elementType": "labels.text.stroke",
    "stylers": [
      {
        "color": "#242f3e"
      }
    ]
  },
  {
    "featureType": "administrative.land_parcel",
    "elementType": "labels",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "administrative.locality",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#d59563"
      }
    ]
  },
  {
    "featureType": "poi",
    "elementType": "labels.text",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "poi",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#d59563"
      }
    ]
  },
  {
    "featureType": "poi.business",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "poi.park",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#263c3f"
      }
    ]
  },
  {
    "featureType": "poi.park",
    "elementType": "labels.text",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "poi.park",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#6b9a76"
      }
    ]
  },
  {
    "featureType": "road",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#38414e"
      }
    ]
  },
  {
    "featureType": "road",
    "elementType": "geometry.stroke",
    "stylers": [
      {
        "color": "#212a37"
      }
    ]
  },
  {
    "featureType": "road",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#9ca5b3"
      }
    ]
  },
  {
    "featureType": "road.highway",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#746855"
      }
    ]
  },
  {
    "featureType": "road.highway",
    "elementType": "geometry.stroke",
    "stylers": [
      {
        "color": "#1f2835"
      }
    ]
  },
  {
    "featureType": "road.highway",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#f3d19c"
      }
    ]
  },
  {
    "featureType": "road.highway.controlled_access",
    "stylers": [
      {
        "visibility": "simplified"
      }
    ]
  },
  {
    "featureType": "road.highway.controlled_access",
    "elementType": "geometry",
    "stylers": [
      {
        "visibility": "simplified"
      }
    ]
  },
  {
    "featureType": "road.highway.controlled_access",
    "elementType": "labels",
    "stylers": [
      {
        "visibility": "on"
      }
    ]
  },
  {
    "featureType": "road.local",
    "elementType": "labels",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "transit",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#2f3948"
      }
    ]
  },
  {
    "featureType": "transit.station",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#d59563"
      }
    ]
  },
  {
    "featureType": "water",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#17263c"
      }
    ]
  },
  {
    "featureType": "water",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#515c6d"
      }
    ]
  },
  {
    "featureType": "water",
    "elementType": "labels.text.stroke",
    "stylers": [
      {
        "color": "#17263c"
      }
    ]
  }
]
    """.trimIndent()
}