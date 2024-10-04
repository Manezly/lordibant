package com.example.audiesafe1

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.activity.result.ActivityResultLauncher
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat


class MainActivity : ComponentActivity() {
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var phoneStatePermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)

        // Notification permissions
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted, proceed with notification setup
                setupPushNotifications()
            } else {
                // Permission is denied, show a message to the user
                notifyUser("Permission for notifications denied.")
            }
        }

       // Phone build permissions
        phoneStatePermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Phone State Permission Granted", Toast.LENGTH_SHORT).show()
                collectAndSendDeviceInfo()
            } else {
                Toast.makeText(this, "Phone State Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
        // Check if the notification permission is needed
        checkAndRequestPermissions()
    }
//    private fun checkAndRequestNotificationPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {  // Android 13 and above
//            when {
//                ContextCompat.checkSelfPermission(
//                    this, Manifest.permission.POST_NOTIFICATIONS
//                ) == PackageManager.PERMISSION_GRANTED -> {
//                    // Permission already granted, proceed with notification setup
//                    setupPushNotifications()
//                }
//                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
//                    // Show a rationale for the request before prompting the user
//                    showNotificationPermissionRationale()
//                }
//                else -> {
//                    // Directly request the notification permission
//                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
//                }
//            }
//        } else {
//            // Permissions are automatically granted for earlier versions
//            setupPushNotifications()
//        }
//    }

    private fun checkAndRequestPermissions() {
        // Check for Notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Request notification permission
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Notifications permission already granted
                Toast.makeText(this, "Notification Permission Already Granted", Toast.LENGTH_SHORT).show()
            }
        }

        // Check for Phone State permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // Request phone state permission
            phoneStatePermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
        } else {
            // Phone state permission already granted
//            collectAndSendDeviceInfo()
        }
    }

    private fun collectAndSendDeviceInfo() {
        val deviceInfo = getDeviceInfo()
        // Use the deviceInfo in your login request
        loginRequest(deviceInfo)
    }

    private fun getDeviceInfo(): Map<String, String> {
        val deviceInfo = mutableMapOf<String, String>()

        // Collect basic device info
        deviceInfo["device_model"] = Build.MODEL ?: "Unknown"
        deviceInfo["manufacturer"] = Build.MANUFACTURER ?: "Unknown"
        deviceInfo["os_version"] = Build.VERSION.RELEASE ?: "Unknown"
        deviceInfo["sdk_version"] = Build.VERSION.SDK_INT.toString()

        // Android ID - no special permission needed
        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        deviceInfo["android_id"] = androidId ?: "Unknown"

        // Optionally collect IMEI if permission granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
            deviceInfo["imei"] = telephonyManager.imei ?: "Unknown" // Might be null in modern phones
        }

        return deviceInfo
    }

    private fun showNotificationPermissionRationale() {
        // Explain to user why permission is needed
        // After the user agrees, request permission again
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun setupPushNotifications() {
        // Put setup code here
    }

    private fun notifyUser(message: String) {
        // Show a message to the user?
    }
    private fun loginRequest(deviceInfo: Map<String, String>) {
        // Implement your login request logic here, including deviceInfo
    }
}
