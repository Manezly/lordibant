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

class MainActivity : ComponentActivity() {


    private val client = OkHttpClient() // OkHttp client to make network requests
    private val interval: Long = 2500 // 2.5 seconds for polling
    private val handler = Handler() // Handler to schedule periodic tasks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)

        val emailInput: EditText = findViewById(R.id.emailInput)
        val loginButton: Button = findViewById(R.id.loginButton)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()

            if (email.isNotEmpty()) {
                loginUser(email)
            } else {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            }
        }

        // Start fetching data every 2.5 seconds
        startRepeatingTask()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop the polling task when the activity is destroyed
        stopRepeatingTask()
    }

    private fun loginUser(email: String) {
        val intent = Intent(this, HomePageActivity::class.java)
        startActivity(intent)

        finish()
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
        setContentView(R.layout.layout)

        // Only call findViewById if the layout contains the views
        val emailInput: EditText = findViewById(R.id.emailInput)
        val loginButton: Button = findViewById(R.id.loginButton)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()

            if (email.isNotEmpty()) {
                loginUser(email)
            } else {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            }
        }
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
