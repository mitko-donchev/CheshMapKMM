package com.epicmillennium.cheshmap.maps

import androidx.compose.runtime.Composable

@Composable
expect fun GoogleMaps(
    userLocation: Location,
    isMapOptionSwitchesVisible: Boolean,
    shouldCenterCameraOnLatLong: LatLong?,
    shouldSetInitialCameraPosition: CameraPosition?,
    onFindMeButtonClick: (() -> Unit)? = null,
)