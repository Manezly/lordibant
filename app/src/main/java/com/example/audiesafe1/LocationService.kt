package com.example.audiesafe1

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import android.Manifest
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class LocationService : Service() {

    private val client = OkHttpClient()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()

        Log.d("SOS", "LocationService created.")

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Set up location callback to receive updates
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.locations.isNotEmpty()) {
                    for (location in locationResult.locations) {
                        logLocation(location) // Log each location update
                    }
                } else {
                    Log.e("SOS", "Received empty location result.")
                }
            }
        }

        // Start location updates
        startLocationUpdates()
    }

    // Function to log latitude and longitude with timestamp
    private fun logLocation(location: Location) {
        // Log the updated latitude and longitude with the current time
        Log.d("SOS", "Updated at ${System.currentTimeMillis()}: Latitude: ${location.latitude}, Longitude: ${location.longitude}")

        // Prepare JSON data to send
        val json = """
            {
                "latitude": ${location.latitude},
                "longitude": ${location.longitude}
            }
        """.trimIndent()

        // Make a POST request to send the data
        Thread {
            postLocationData(json)
        }.start() // Start the thread
    }

    private fun postLocationData(json: String) {
        val url = "http://192.168.0.218:3000/add-coordinates" // Replace with your actual API endpoint
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = RequestBody.create(mediaType, json)

        // Build the request
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            // Execute the request
            val response: Response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Log.d("SOS", "Location data posted successfully: ${response.body?.string()}")
            } else {
                Log.e("SOS", "Failed to post location data: ${response.code}")
            }
        } catch (e: IOException) {
            Log.e("SOS", "Error posting location data: ${e.message}")
        }
    }

    // Function to start location updates
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 5000 // Update interval in milliseconds
        ).setMinUpdateIntervalMillis(2000).build() // Minimum update interval in milliseconds

        // Check if permissions are granted before requesting location updates
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("SOS", "Requesting location updates.")
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } else {
            Log.e("SOS", "Location permissions are not granted.") // Log error if permissions are missing
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Check if the intent action is to stop the service
        if (intent?.action == "STOP_LOCATION_SERVICE") {
            stopLocationService() // Call the function to stop location updates and the service
            return START_NOT_STICKY
        }

        // If not, proceed with starting the location updates
        Log.d("SOS", "LocationService started.")
        val notification = createNotification()
        startForeground(1, notification)

        return START_STICKY
    }

    // Function to stop location tracking and the foreground service
    private fun stopLocationService() {
        Log.d("SOS", "Stopping LocationService.")

        // Remove location updates
        fusedLocationClient.removeLocationUpdates(locationCallback)

        // Stop the foreground notification and service
        stopForeground(true)
        stopSelf() // Stops the service
    }


    private fun createNotification(): Notification {
        val channelId = "location_channel"
        val channelName = "Location Service"

        // For Android 8.0 (API 26) and above, we need to create a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT // Ensure the importance is set properly
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(notificationChannel)
        }

        // Create the notification
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Audiesafe")
            .setContentText("You are checked into Loneworker")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ensure you're using a valid icon resource
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Set priority for older versions
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback) // Stop location updates when service is destroyed
    }
}
