package com.epicmillennium.cheshmap

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.maps.MapsInitializer

class MainActivity : ComponentActivity() {

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup the permission launcher & callback
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            // Check if permissions were granted
            if (it[Manifest.permission.ACCESS_FINE_LOCATION] == false ||
                it[Manifest.permission.ACCESS_COARSE_LOCATION] == false
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Location Permissions Required")
                    .setMessage(
                        "This app requires location permissions to function. " +
                                "Please enable location permissions in the app settings."
                    )
                    .setPositiveButton("App Settings") { _, _ ->
                        // Intent to open the App Settings
                        Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.parse("package:$packageName")
                            startActivity(this)
                        }
                    }
                    .setNegativeButton("Cancel") { _, _ ->

                        // enable logging:  adb shell setprop log.tag.FA VERBOSE
                        //                  adb shell setprop log.tag.FA-SVC VERBOSE
                        //                  adb logcat -v time -s FA FA-SVC
                        // disable logging: adb shell setprop debug.firebase.analytics.app .none.
//                        firebaseAnalytics.logEvent(
//                            "location_granted",
//                            Bundle().apply {
//                                putString("granted", "false")
//                            }
//                        )

                        finish()
                    }
                    .show()
            } else {
                // Permissions Granted -> Dismiss the splash screen
//                splashState.tryEmit(false)
//                appSettings.isPermissionsGranted = true

                setContent {
                    App()
                }
            }
        }
        // Get permissions to access location (opens dialog)
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        )

        // Initialize Google Maps SDK
        // See https://issuetracker.google.com/issues/228091313
        MapsInitializer.initialize(this, MapsInitializer.Renderer.LATEST) {
            Log.i(
                this.toString(),
                "onMapsSdkInitialized: initialized Google Maps SDK, version: ${it.name}"
            )
        }

        appContext = applicationContext
    }
}