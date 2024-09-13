package com.epicmillennium.cheshmap.maps

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLHeading
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLDistanceFilterNone
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.CoreLocation.kCLLocationAccuracyBestForNavigation
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.concurrent.AtomicReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import co.touchlab.kermit.Logger as Log

actual class GPSService {

    // Define a native CLLocationManager object
    private val locationManager = CLLocationManager()
    private val oneTimeLocationManager = CLLocationManager()
    private val locationDelegate = LocationDelegate()

    // Define an atomic reference to store the latest location
    private val latestLocation = AtomicReference<Location?>(null)

    // Define a custom delegate that extends NSObject and implements CLLocationManagerDelegateProtocol
    private class LocationDelegate : NSObject(), CLLocationManagerDelegateProtocol {

        // Define a callback to receive location updates
        var onLocationUpdate: ((Location?) -> Unit)? = null

        @OptIn(ExperimentalForeignApi::class)
        override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
            didUpdateLocations.firstOrNull()?.let {
                val location = it as CLLocation
                location.coordinate.useContents {
                    onLocationUpdate?.invoke(Location(latitude, longitude))
                }

            }
        }

        override fun locationManager(manager: CLLocationManager, didUpdateHeading: CLHeading) {
        }

        override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
            Log.w { "Error: ${didFailWithError.localizedFailureReason} ${didFailWithError.localizedDescription}, ${didFailWithError.localizedRecoverySuggestion}" }
            Log.w { "Error: ${didFailWithError.userInfo["timestamp"]}" }
            onLocationUpdate?.invoke(null)
        }

        override fun locationManager(manager: CLLocationManager, didChangeAuthorizationStatus: Int) {
            Log.w { "Authorization status changed to: $didChangeAuthorizationStatus" }
        }

        override fun locationManagerDidPauseLocationUpdates(manager: CLLocationManager) {
            Log.d { "locationManagerDidPauseLocationUpdates" }
        }

        override fun locationManagerDidResumeLocationUpdates(manager: CLLocationManager) {
            Log.d { "locationManagerDidResumeLocationUpdates" }
        }
    }

    actual suspend fun getCurrentGPSLocationOneTime(): Location = suspendCoroutine { continuation ->
        oneTimeLocationManager.requestWhenInUseAuthorization()
        oneTimeLocationManager.desiredAccuracy = kCLLocationAccuracyBest

        oneTimeLocationManager.startUpdatingLocation()

        // Define a callback to receive location updates
        val locationDelegate = LocationDelegate()
        locationDelegate.onLocationUpdate = { location ->
            oneTimeLocationManager.stopUpdatingLocation()
            latestLocation.getAndSet(location)

            location?.run {
                continuation.resume(this)
            } ?: run {
                continuation.resumeWithException(Exception("Unable to get current location - 2"))
            }
        }
        oneTimeLocationManager.delegate = locationDelegate
    }

    actual suspend fun onUpdatedGPSLocation(
        errorCallback: (String) -> Unit,
        locationCallback: (Location?) -> Unit
    ) {
        locationManager.requestWhenInUseAuthorization()  // for background location updates
        locationDelegate.onLocationUpdate = locationCallback
        locationManager.delegate = locationDelegate

        locationManager.pausesLocationUpdatesAutomatically = true  // to save battery
        locationManager.showsBackgroundLocationIndicator = true // required for background location updates
        locationManager.allowsBackgroundLocationUpdates = true // required for background location updates
        locationManager.distanceFilter = kCLDistanceFilterNone
        locationManager.desiredAccuracy = kCLLocationAccuracyBestForNavigation
        locationManager.startUpdatingLocation()
    }

    actual fun getLatestGPSLocation(): Location? = latestLocation.value

    actual fun allowBackgroundLocationUpdates() {
        locationManager.allowsBackgroundLocationUpdates = true
    }

    actual fun preventBackgroundLocationUpdates() {
        locationManager.allowsBackgroundLocationUpdates = false
    }
}