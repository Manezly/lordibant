package com.example.audiesafe1

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import android.net.Uri

class HomePageActivity : AppCompatActivity() {
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
    }

    private fun openSideMenu() {
        drawerLayout.openDrawer(GravityCompat.START)
    }

    private fun openHistoryPage() {
        // Start the HistoryActivity when the bell button is clicked
        val intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)
    }
}