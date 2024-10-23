package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query

class RegisterBusinessActivity : AppCompatActivity() {

    private lateinit var txtBusinessName: EditText
    private lateinit var btnRegBusiness: Button
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_business)

        txtBusinessName = findViewById(R.id.txtRegBusinessName)
        btnRegBusiness = findViewById(R.id.btnRegisterBusiness)

        // Initialize the Firebase database reference
        database = FirebaseDatabase.getInstance().reference

        btnRegBusiness.setOnClickListener {
            registerBusiness()
        }
    }

    private fun registerBusiness() {
        val businessName = txtBusinessName.text.toString().trim()

        if (businessName.isEmpty()) {
            Toast.makeText(this, "Please enter a business name.", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if the business name already exists in the database
        val query: Query = database.child("Users").orderByChild("businessName").equalTo(businessName)
        query.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (task.result?.exists() == true) {
                    // Business name already exists
                    Toast.makeText(this, "This business name is already taken. Please choose another.", Toast.LENGTH_SHORT).show()
                } else {
                    // Business name is available, save it
                    saveBusinessName(businessName)
                }
            } else {
                Toast.makeText(this, "Error checking business name.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveBusinessName(businessName: String) {
    //    val userId = "exampleUserId" // Replace this with actual user ID logic, e.g., FirebaseAuth.getInstance().currentUser?.uid

        // Create a node for Users and use the userId as the key
        val userNode = database.child("Users")

        // Set the business name directly under the user's node
        userNode.child("businessName").setValue(businessName).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Business registered successfully!", Toast.LENGTH_SHORT).show()
                // Optionally, redirect or finish the activity
            } else {
                Toast.makeText(this, "Error registering business name.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
