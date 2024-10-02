package com.example.audiesafe1

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import android.net.Uri
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class HomePageActivity : AppCompatActivity() {
    private val client = OkHttpClient() // OkHttp client to make network requests
    private val interval: Long = 2500 // 2.5 seconds for polling
    private val handler = Handler() // Handler to schedule periodic tasks
    private lateinit var drawerLayout: DrawerLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_page)

        drawerLayout = findViewById(R.id.drawer_layout)

        val burgerButton: ImageButton = findViewById(R.id.burgerButton)
        burgerButton.setOnClickListener {
            openSideMenu()
        }

        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.privacy_policy -> {
                    // Open Privacy Policy
                    val intent = Intent(this, PrivacyPolicyActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.terms_conditions -> {
                    // Open Terms and Conditions
                    val intent = Intent(this, TermsAndConditionsActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.logout -> {
                    // Handle logout
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> false
            }
        }

        val historyButton: ImageButton = findViewById(R.id.historyButton)


        historyButton.setOnClickListener{
            openHistoryPage()
        }

        // Start fetching data every 2.5 seconds
        startRepeatingTask()
    }

    private fun openSideMenu() {
        drawerLayout.openDrawer(GravityCompat.START)
    }

    private fun openHistoryPage() {
        // Start the HistoryActivity when the bell button is clicked
        val intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop the polling task when the activity is destroyed
        stopRepeatingTask()
    }

    private fun fetchDummyData() {
        val request = Request.Builder()
            .url("http://10.0.2.2:3000/dummy-data") // Local server URL
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FetchError", "Request failed: ${e.message}")
                // Show default screen if the request fails
                runOnUiThread {
                    showDefaultScreen()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonResponse = JSONObject(responseBody)
                    val dataArray = jsonResponse.getJSONArray("data")

                    // Check if the array is empty
                    if (dataArray.length() > 0) {
                        runOnUiThread {
                            showRedScreen() // Show red screen if array has elements
                        }
                    } else {
                        runOnUiThread {
                            showDefaultScreen() // Show default screen if array is empty
                        }
                    }
                } else {
                    Log.e("FetchError", "Failed to fetch data: ${response.code}")
                    // Show default screen if response is not successful
                    runOnUiThread {
                        showDefaultScreen()
                    }
                }
            }
        })
    }


    private fun showRedScreen() {
        // Set the red screen layout
        setContentView(R.layout.red_screen_layout)
    }

    private fun showDefaultScreen() {
        // Set the default layout back
        setContentView(R.layout.home_page)

    }

    // Handler for repeating task (fetch data every 2.5 seconds)
    private val fetchDataRunnable = object : Runnable {
        override fun run() {
            fetchDummyData() // Fetch dummy data from server
            handler.postDelayed(this, interval) // Schedule the next execution
        }
    }

    // Start polling task
    private fun startRepeatingTask() {
        handler.post(fetchDataRunnable)
    }

    // Stop polling task
    private fun stopRepeatingTask() {
        handler.removeCallbacks(fetchDataRunnable)
    }
}