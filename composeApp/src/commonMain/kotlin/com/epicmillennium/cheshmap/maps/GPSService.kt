package com.epicmillennium.cheshmap.maps

expect class GPSService {
    suspend fun getCurrentGPSLocationOneTime(): Location
    suspend fun onUpdatedGPSLocation(
        errorCallback: (String) -> Unit = {},
        locationCallback: (Location?) -> Unit
    )
    fun getLatestGPSLocation(): Location?

    fun allowBackgroundLocationUpdates()
    fun preventBackgroundLocationUpdates()
}