package com.example.audiesafe1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.activity.result.ActivityResultLauncher

class MainActivity : AppCompatActivity() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)


        val continueButton: Button = findViewById(R.id.loginButton)

        continueButton.setOnClickListener {
            // Create an Intent to start the HomePageActivity
            val intent = Intent(this, HomePageActivity::class.java)
            startActivity(intent) // Start the HomePageActivity
        }

        // Initialize the permission launcher
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted, proceed with notification setup
                setupPushNotifications()
            } else {
                // Permission is denied, notify the user
                notifyUser("Permission for notifications denied.")
            }
        }

        // Check if the notification permission is needed
        checkAndRequestNotificationPermission()
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {  // Android 13 and above
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted, proceed with notification setup
                    setupPushNotifications()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Show a rationale for the request before prompting the user
                    showNotificationPermissionRationale()
                }
                else -> {
                    // Directly request the notification permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Permissions are automatically granted for earlier versions
            setupPushNotifications()
        }
    }

    private fun showNotificationPermissionRationale() {
        // Show a dialog explaining why the app needs notification permission
        AlertDialog.Builder(this)
            .setTitle("Notification Permission Required")
            .setMessage("This app requires notifications to send you important messages, such as alerts and updates. Please enable notifications to ensure you stay informed.")
            .setPositiveButton("Allow") { _, _ ->
                // Request permission again after explanation
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            .setNegativeButton("Deny") { _, _ ->
                // Notify the user about the denial
                notifyUser("You won't receive notifications without permission.")
            }
            .setCancelable(false)
            .show()
    }

    private fun setupPushNotifications() {
        // Initialize your push notifications logic here
        Toast.makeText(this, "Push notifications enabled", Toast.LENGTH_SHORT).show()
    }

    private fun notifyUser(message: String) {
        // Show a message to the user, such as a Toast or Snackbar
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
