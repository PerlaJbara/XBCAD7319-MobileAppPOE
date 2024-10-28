package com.opsc7311poe.xbcad_antoniemotors

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class AdminSelectBusinessActivity : AppCompatActivity() {

    private lateinit var spBusinessNames: Spinner
    private lateinit var btnAdminSelBusiness: Button
    private lateinit var businessNamesList: MutableList<String>
    private lateinit var databaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_select_business)

        spBusinessNames = findViewById(R.id.spAllBusinessNames)
        btnAdminSelBusiness = findViewById(R.id.btnAdminBusChoice)
        businessNamesList = mutableListOf()
        databaseRef = FirebaseDatabase.getInstance().getReference("Users")

        // Load business names into spinner
        loadBusinessNames()

        // Button click listener to carry the selected business name to the next page
        btnAdminSelBusiness.setOnClickListener {
            val selectedBusinessName = spBusinessNames.selectedItem.toString()
            val intent = Intent(this, AdminEnterInfo::class.java)
            intent.putExtra("selectedBusinessName", selectedBusinessName)
            startActivity(intent)
        }
    }

    // Function to load business names from Firebase
    private fun loadBusinessNames() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                businessNamesList.clear() // Clear the list before adding new data

                // Iterate through Users
                for (userSnapshot in snapshot.children) {
                    val businessInfoSnapshot = userSnapshot.child("BusinessInfo")
                    val businessName = businessInfoSnapshot.child("businessName").getValue(String::class.java)

                    // Check if the business name is not null
                    businessName?.let {
                        businessNamesList.add(it)
                    }
                }

                // Populate the spinner with business names
                val adapter = ArrayAdapter(this@AdminSelectBusinessActivity, android.R.layout.simple_spinner_item, businessNamesList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spBusinessNames.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminSelectBusinessActivity, "Failed to load business names: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
