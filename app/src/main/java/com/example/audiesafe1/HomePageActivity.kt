package com.example.audiesafe1

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class HomePageActivity : AppCompatActivity() {
    private val client = OkHttpClient() // OkHttp client to make network requests
    private val handler = Handler() // Handler to schedule periodic tasks
    private val email = "leslie.leung@audiebant.co.uk" // Example email, replace with dynamic user email if needed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_page)

        // Fetch data when the activity is created
        fetchData(email)
    }

    private fun fetchData(email: String) {
        // Your actual API URL with email parameter
        val url = "https://audiesafe.audiebant.co.uk/audiesafe_notifications_api.php?email=$email"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FetchError", "Request failed: ${e.message}")
                // Handle failure (you might want to show a Toast or dialog here)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()

                    // Parse the JSON array response
                    val jsonArray = JSONArray(responseBody)

                    // Update the UI on the main thread
                    runOnUiThread {
                        if (jsonArray.length() > 0) {
                            // If data is present, show dynamic content
                            updateUI(jsonArray)
                        } else {
                            // If data is empty, show static content
                            showStaticContent()
                        }
                    }
                } else {
                    Log.e("FetchError", "Failed to fetch data: ${response.code}")
                }
            }
        })
    }

    private fun updateUI(dataArray: JSONArray) {
        val staticContentLayout: View = findViewById(R.id.staticContentLayout)
        val scrollView: ScrollView = findViewById(R.id.scrollView)
        val dynamicDataContainer: LinearLayout = findViewById(R.id.dynamicDataContainer)

        // Hide the static content and show the dynamic content
        staticContentLayout.visibility = View.GONE
        scrollView.visibility = View.VISIBLE

        // Clear any previous dynamic data
        dynamicDataContainer.removeAllViews()

        // Loop through the array and add each item to the UI dynamically
        for (i in 0 until dataArray.length()) {
            val item: JSONObject = dataArray.getJSONObject(i)

            // Safely get the values from the JSON object
            val date = if (item.has("Date")) item.getString("Date") else "Unknown Date"
            val text = if (item.has("Text")) item.getString("Text") else "Unknown Text"
            val partOf = if (item.has("PartOf")) item.getString("PartOf") else "Unknown Part Of"

            // Inflate a new view for each item
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_data, dynamicDataContainer, false)

            // Set the values in the inflated layout
            val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
            val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
            val partOfTextView: TextView = itemView.findViewById(R.id.partOfTextView)

            dateTextView.text = "Date: $date"
            nameTextView.text = "Text: $text"
            partOfTextView.text = "Part Of: $partOf"

            // Set an OnClickListener to show a dialog when clicked
            itemView.setOnClickListener {
                showMessageDialog(text) // Pass the message (Text) to the dialog
            }

            // Add the inflated view to the container
            dynamicDataContainer.addView(itemView)
        }
    }

    private fun showMessageDialog(message: String) {
        // Create a custom dialog
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_message, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        val dialog = dialogBuilder.create()

        // Find the TextView and Button in the dialog layout
        val dialogMessageText: TextView = dialogView.findViewById(R.id.dialogMessageText)
        val closeButton: Button = dialogView.findViewById(R.id.closeButton)

        // Set the message in the dialog
        dialogMessageText.text = message

        // Set click listener for the close button
        closeButton.setOnClickListener {
            dialog.dismiss() // Close the dialog when the button is clicked
        }

        // Show the dialog
        dialog.show()
    }


    private fun showStaticContent() {
        val staticContentLayout: View = findViewById(R.id.staticContentLayout)
        val scrollView: ScrollView = findViewById(R.id.scrollView)

        // Show the static content and hide the dynamic content
        staticContentLayout.visibility = View.VISIBLE
        scrollView.visibility = View.GONE
    }
}
