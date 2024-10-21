package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavView: BottomNavigationView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavView = findViewById(R.id.bottom_navigation)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Check if the user is logged in
        val user = auth.currentUser
        if (user != null) {
            // User is logged in, fetch the role
            getUserRole(user.uid)
        } else {
            // Handle case where the user is not logged in, e.g., show a login screen
            Log.e("MainActivity", "User is not logged in")
            // You can navigate to the login screen if needed.
        }

        // Set up the default fragment (for example, HomeFragment)
        replaceFragment(HomeFragment())

        bottomNavView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navHome -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.navDocument -> {
                    replaceFragment(DocumentationFragment())
                    true
                }
                R.id.navEmployees -> {
                    replaceFragment(EmployeeFragment())
                    true
                }
                R.id.navEmpHome -> {
                    replaceFragment(EmployeeHomeNav())
                    true
                }
                R.id.navVehicles -> {
                    replaceFragment(VehicleMenuFragment())
                    true
                }
                R.id.navCustomers -> {
                    replaceFragment(CustomerFragment())
                    true
                }
                R.id.navTasks -> {
                    replaceFragment(TasksFragment()) // For employee role
                    true
                }
                R.id.navEmpHome -> {
                    replaceFragment(EmployeeHomeFragment()) // For employee role
                    true
                }
                R.id.navLeave -> {
                    replaceFragment(LeaveFragment()) // For employee role
                    true
                }
                else -> false
            }
        }
    }

    private fun getUserRole(userId: String) {
        // Get the role from Firebase Realtime Database
        val userRef = database.child("Users").child(userId).child("role")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userRole = snapshot.getValue(String::class.java)
                if (userRole != null) {
                    loadNavigationMenu(userRole)
                    // Set the default fragment after loading the menu
                    replaceFragment(HomeFragment())
                } else {
                    Log.e("MainActivity", "Role not found for user")
                    // Handle if the role is not found
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Failed to fetch user role: ${error.message}")
            }
        })
    }


    private fun loadNavigationMenu(userRole: String) {
        // Load the appropriate menu based on the role
        when (userRole.lowercase()) {
            "admin" -> {
                bottomNavView.menu.clear()
                bottomNavView.inflateMenu(R.menu.menu) // Load admin menu
            }
            "employee" -> {
                bottomNavView.menu.clear()
                bottomNavView.inflateMenu(R.menu.empmenu) // Load employee menu
            }
            else -> {
                Log.w("MainActivity", "Unknown user role: $userRole")
                bottomNavView.menu.clear()
                bottomNavView.inflateMenu(R.menu.empmenu) // Load a guest menu or default behavior
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.fragment_slide_in_right,   // Enter animation
                R.anim.fragment_slide_out_left,   // Exit animation
                R.anim.fragment_slide_in_left,    // Pop enter animation
                R.anim.fragment_slide_out_right   // Pop exit animation
            )
            .replace(R.id.frame_container, fragment)
            .commit()
    }

}
