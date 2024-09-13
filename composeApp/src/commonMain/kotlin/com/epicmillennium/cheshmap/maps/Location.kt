package com.epicmillennium.cheshmap.maps

import kotlinx.serialization.Serializable

// Define a data class to store latitude and longitude coordinates
@Serializable
data class Location(val latitude: Double, val longitude: Double)

fun Location.isLocationValid() = latitude != 0.0 && longitude != 0.0

fun LatLong.toLocation() = Location(latitude, longitude)