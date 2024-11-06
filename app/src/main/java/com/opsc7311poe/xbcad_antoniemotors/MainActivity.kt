package com.opsc7311poe.xbcad_antoniemotors

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var businessId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        businessId = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).getString("business_id", null)!!
        Log.e("MainActivity", "BusinessID fecthed: $businessId")

        bottomNavView = findViewById(R.id.bottom_navigation)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Check if the user is logged in
        val user = auth.currentUser
        if (user != null) {
            // User is logged in, fetch the role and approval status
            getUserDetails(user.uid)

        } else {
            Log.e("MainActivity", "User is not logged in")
            Toast.makeText(this,"You're not logged in!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this@MainActivity, Login ::class.java)
            startActivity(intent)
            finish()
        }

        // Set up the bottom navigation listener
        bottomNavView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navHome -> {
                    replaceFragment(HomeFragment())
                    Log.d("Navbar", "Going to HomeFragment")
                    true
                }
                R.id.navDocument -> {
                    replaceFragment(DocumentationFragment())
                    true
                }
                R.id.navEmployees -> {
                 //   replaceFragment(EmployeeFragment())
                 replaceFragment(AdminApproveRegistrationFragment())
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
                    Log.d("Navbar", "Going to EmployeeHomeFragment")
                    true
                }
                R.id.navLeave -> {
                    replaceFragment(EmpLeaveFragment()) // For employee role
                    true
                }
                R.id.navMessages -> {
                    replaceFragment(EmpProfileFragment()) // For employee role
                    true
                }
                R.id.navLeaderboard -> {
                    replaceFragment(LeaderboardFragment()) // For employee role
                    true
                }
                else -> false
            }
        }
    }

    private fun getUserDetails(userId: String) {
        // Get the role and approval status from Firebase Realtime Database
        val userRef = database.child("Users").child(businessId).child("Employees").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userRole = snapshot.child("role").getValue(String::class.java)
                val approvalStatus = snapshot.child("approval").getValue(String::class.java)

                if (userRole != null && approvalStatus != null) {
                    when (approvalStatus) {
                        "approved" -> {
                            // Load the appropriate navigation bar based on the role
                            loadNavigationMenu(userRole)

                            // Set the default fragment
                            when (userRole.lowercase()) {
                                "admin", "owner" -> replaceFragment(HomeFragment()) // For admin/owner
                                "employee" -> replaceFragment(EmployeeHomeFragment()) // For employee
                            }
                        }
                        "pending" -> {
                            // Redirect to WaitingActivity
                            val intent = Intent(this@MainActivity, WaitingActivity::class.java)
                            startActivity(intent)
                            finish() // Close the MainActivity
                        }
                        "denied" -> {
                            // Redirect to DeniedActivity
                            val intent = Intent(this@MainActivity, DeniedActivity::class.java)
                            startActivity(intent)
                            finish() // Close the MainActivity
                        }
                        else -> {
                            Log.e("MainActivity", "Unknown approval status: $approvalStatus")
                        }
                    }
                } else {
                    Log.e("MainActivity", "Role or approval status not found for user")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Failed to fetch user details: ${error.message}")
            }
        })
    }

    private fun loadNavigationMenu(userRole: String) {
        // Load the appropriate menu based on the role
        when (userRole.lowercase()) {
            "admin", "owner" -> {
                bottomNavView.menu.clear()
                bottomNavView.inflateMenu(R.menu.menu) // Load admin/owner menu
            }
            "employee" -> {
                bottomNavView.menu.clear()
                bottomNavView.inflateMenu(R.menu.empmenu) // Load employee menu
            }
            else -> {
                Log.w("MainActivity", "Unknown user role: $userRole")
                bottomNavView.menu.clear()
                bottomNavView.inflateMenu(R.menu.empmenu) // Load a default menu for unknown roles
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
