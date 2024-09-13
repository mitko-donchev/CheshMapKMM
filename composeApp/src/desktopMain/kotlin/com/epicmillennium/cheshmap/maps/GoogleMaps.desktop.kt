package com.epicmillennium.cheshmap.maps

import androidx.compose.runtime.Composable

@Composable
actual fun GoogleMaps(
    userLocation: Location,
    isMapOptionSwitchesVisible: Boolean,
    shouldCenterCameraOnLatLong: LatLong?,
    shouldSetInitialCameraPosition: CameraPosition?,
    onFindMeButtonClick: (() -> Unit)?,
) {
}