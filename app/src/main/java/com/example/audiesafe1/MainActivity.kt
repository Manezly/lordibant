package com.example.audiesafe1

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

class MainActivity : AppCompatActivity() {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Create LocationRequest with the Builder
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setWaitForAccurateLocation(false) // Get immediate location updates
            .setMinUpdateIntervalMillis(5000) // Minimum update interval
            .build()

        // Initialize location callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                // Log all updated locations
                for (location in locationResult.locations) {
                    if (location != null) {
                        // Log updated Latitude and Longitude
                        Log.d("SOS", "Updated Latitude: ${location.latitude}, Longitude: ${location.longitude}")
                    } else {
                        Log.d("SOS", "Received null location update.")
                    }
                }
            }
        }

        // Setup SOS button click listener
        val sosButton: Button = findViewById(R.id.sosButton)
        sosButton.setOnClickListener {
            Log.d("SOS", "SOS Button Pressed")
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                requestNewLocation()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    // Function to request new location updates
    private fun requestNewLocation() {
        Log.d("SOS", "Requesting new location updates...")
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("SOS", "Location permission not granted. Cannot request location updates.")
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    // Handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("SOS", "Location permission granted")
                requestNewLocation()
            } else {
                Log.d("SOS", "Location permission denied")
            }
        }
    }
}
