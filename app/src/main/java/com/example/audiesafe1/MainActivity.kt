package com.example.audiesafe1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.location.* // Import LocationCallback, LocationRequest, etc.
import android.os.Build
import android.view.View
import android.app.AlertDialog

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        }

        // Initialize fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Set up the LocationCallback for continuous location updates
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult != null) {
                    for (location in locationResult.locations) {
                        Log.d("SOS", "Updated Latitude: ${location.latitude}, Longitude: ${location.longitude}")
                    }
                } else {
                    Log.d("SOS", "Location is null")
                }
            }
        }

        val sosButton = findViewById<Button>(R.id.sosButton)
        val signOutButton = findViewById<Button>(R.id.signOutButton) // Assuming the button exists in your layout
        sosButton.setOnClickListener {
            showStartConfirmationDialog(sosButton, signOutButton)
        }

        signOutButton.setOnClickListener {
            showStopConfirmationDialog(sosButton, signOutButton)
        }
    }

    // Show a confirmation dialog for starting the service
    private fun showStartConfirmationDialog(sosButton: Button, signOutButton: Button) {
        AlertDialog.Builder(this).apply {
            setTitle("Start Location Service")
            setMessage("Are you sure you want to start location tracking?")
            setPositiveButton("Yes") { _, _ ->
                // If confirmed, start the location service
                if (isLocationEnabled()) {
                    if (ActivityCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Start the foreground service for continuous location tracking
                        val serviceIntent = Intent(this@MainActivity, LocationService::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(serviceIntent)
                        } else {
                            startService(serviceIntent)
                        }

                        // On successful start, hide the SOS button and show the Sign Out button
                        sosButton.visibility = View.GONE
                        signOutButton.visibility = View.VISIBLE

                    } else {
                        requestLocationPermissions()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Please turn on the location.", Toast.LENGTH_SHORT).show()
                }
            }
            setNegativeButton("No") { dialog, _ ->
                dialog.dismiss() // Dismiss the dialog if the user cancels
            }
        }.show()
    }

    // Show a confirmation dialog for stopping the service
    private fun showStopConfirmationDialog(sosButton: Button, signOutButton: Button) {
        AlertDialog.Builder(this).apply {
            setTitle("Stop Location Service")
            setMessage("Are you sure you want to stop location tracking?")
            setPositiveButton("Yes") { _, _ ->
                // If confirmed, stop the location service
                val stopServiceIntent = Intent(this@MainActivity, LocationService::class.java)
                stopServiceIntent.action = "STOP_LOCATION_SERVICE" // Custom action to stop the service

                // Stop the service
                stopService(stopServiceIntent)

                // On successful stop, hide the Sign Out button and show the SOS button
                signOutButton.visibility = View.GONE
                sosButton.visibility = View.VISIBLE
            }
            setNegativeButton("No") { dialog, _ ->
                dialog.dismiss() // Dismiss the dialog if the user cancels
            }
        }.show()
    }

    private fun requestNotificationPermission() {
        val notificationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        notificationPermissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    // Function to check if location services are enabled
    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    // Request location permissions if needed
    private fun requestLocationPermissions() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                // Permissions granted, start the location service
                val serviceIntent = Intent(this, LocationService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
}
