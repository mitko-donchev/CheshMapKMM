package com.epicmillennium.cheshmap.maps

actual class GPSService {
    actual suspend fun getCurrentGPSLocationOneTime(): Location {
        TODO("Not yet implemented")
    }

    actual suspend fun onUpdatedGPSLocation(
        errorCallback: (String) -> Unit,
        locationCallback: (Location?) -> Unit
    ) {
    }

    actual fun getLatestGPSLocation(): Location? {
        TODO("Not yet implemented")
    }

    actual fun allowBackgroundLocationUpdates() {
    }

    actual fun preventBackgroundLocationUpdates() {
    }

}