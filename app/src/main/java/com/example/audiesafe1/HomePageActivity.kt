package com.example.audiesafe1

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class HomePageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_page)

        val burgerButton: ImageButton = findViewById(R.id.burgerButton)
        val historyButton: ImageButton = findViewById(R.id.historyButton)

        burgerButton.setOnClickListener{
            openSideMenu()
        }

        historyButton.setOnClickListener{
            openHistoryPage()
        }
    }

    private fun openSideMenu() {
        // Logic to open your side menu
        // This could be a DrawerLayout or a custom dialog
    }

    private fun openHistoryPage() {
        // Start the HistoryActivity when the bell button is clicked
        val intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)
    }
}