package com.example.audiesafe1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import okhttp3.*
import java.io.IOException
import kotlin.concurrent.thread
import org.json.JSONObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class MainActivity : ComponentActivity() {

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout) // Update with your actual layout file name

        val emailInput: EditText = findViewById(R.id.emailInput)
        val loginButton: Button = findViewById(R.id.loginButton)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()

            if (email.isNotEmpty()) {
                // Call login API
                loginUser(email)
            } else {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(email: String) {
        thread {
            // Create JSON object with email
            val json = JSONObject()
            json.put("email", email)

            // Define JSON MediaType
            val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
            val requestBody = json.toString().toRequestBody(JSON)

            // Create request
            val request = Request.Builder()
                .url("http://10.0.2.2:3000/api/login") // Your local server address
                .post(requestBody)
                .build()

            // Make asynchronous request
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("LoginError", "Request failed: ${e.message}")
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Login failed. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string() // Get response body for logging
                    Log.d("LoginResponse", "Response: $responseBody")

                    if (response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@MainActivity, HomePageActivity::class.java)
                            startActivity(intent)
                        }
                    } else {
                        Log.e("LoginError", "Login failed. Server response: $responseBody")
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Login failed. Invalid email.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            })
        }
}}
