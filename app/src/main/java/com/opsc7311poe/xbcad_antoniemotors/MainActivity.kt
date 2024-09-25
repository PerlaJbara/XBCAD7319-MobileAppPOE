package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavView: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavView = findViewById(R.id.bottom_navigation)

        replaceFragment(HomeFragment())
        bottomNavView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navHome -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.navDocument -> {
                    replaceFragment(ReceiptGeneratorFragment())
                    true
                }
                R.id.navEmployees -> {
                    replaceFragment(EmployeeFragment())
                    true
                }
                R.id.navVehicles -> {
                    replaceFragment(ServicesFragment())
                    true
                }
                R.id.navCustomers -> {
                    replaceFragment(QuoteGenFragment())
                    true
                }
                else -> false
            }
        }


        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())

        }
    }

    private fun replaceFragment(fragment: Fragment) {
        Log.d("MainActivity", "Replacing fragment: ${fragment::class.java.simpleName}")
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}

