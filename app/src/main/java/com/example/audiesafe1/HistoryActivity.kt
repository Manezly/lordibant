package com.example.audiesafe1

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.LinearLayout
import androidx.activity.ComponentActivity

class HistoryActivity : ComponentActivity() {

    // Dummy data
    private val leftArray = listOf("Left Item 1", "Left Item 2", "Left Item 3", "Left Item 4")
    private val rightArray = listOf("Right Item 1", "Right Item 2", "Right Item 3")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history_page)

        val backButton: TextView = findViewById(R.id.backButton)
        val leftButton: Button = findViewById(R.id.leftButton)
        val rightButton: Button = findViewById(R.id.rightButton)
        val dataContainer: LinearLayout = findViewById(R.id.dataContainer)

        // Set up the back button
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Initially set the left button as selected
        leftButton.isSelected = true
        displayData(dataContainer, leftArray)

        leftButton.setOnClickListener {
            if (!it.isSelected) {
                it.isSelected = true
                rightButton.isSelected = false
                displayData(dataContainer, leftArray)
            }
        }

        // Handle the right button click
        rightButton.setOnClickListener {
            if (!it.isSelected) {
                it.isSelected = true
                leftButton.isSelected = false
                displayData(dataContainer, rightArray)
            }
        }

        // Display left array data by default when the screen loads
        displayData(dataContainer, leftArray)
    }

    // Function to display data in the ScrollView
    private fun displayData(container: LinearLayout, data: List<String>) {
        // Clear current views
        container.removeAllViews()

        // Add new views based on the data array
        for (item in data) {
            val textView = TextView(this).apply {
                text = item
                textSize = 18f
                setPadding(16, 16, 16, 16)
            }
            container.addView(textView)
        }
    }
}
