package com.opsc7311poe.xbcad_antoniemotors

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class AdminEnterInfo : AppCompatActivity() {

    private lateinit var txtBusinessName: TextView
    private lateinit var txtAdminFirstName: TextView
    private lateinit var txtAdminLastName: TextView
    private lateinit var txtAdminEmail: TextView
    private lateinit var txtAdminPassword: TextView
    private lateinit var ivProfileImage: ImageView
    private lateinit var txtSelectProfileImage: TextView
    private var selectedImageUri: Uri? = null
    private var role: String? = null
    private lateinit var btnRegisterAdmin: Button

    // Firebase instances
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_enter_info)

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        // Initialize views
        txtBusinessName = findViewById(R.id.txtAdminBusinessName)
        txtAdminFirstName = findViewById(R.id.txtAdminFirstName)
        txtAdminLastName = findViewById(R.id.txtAdminLastName)
        txtAdminEmail = findViewById(R.id.txtAdminEmail)
        txtAdminPassword = findViewById(R.id.txtPassword)
        ivProfileImage = findViewById(R.id.ivAdminProfilePicture)
        txtSelectProfileImage = findViewById(R.id.txtSelectImage)
        btnRegisterAdmin = findViewById(R.id.btnRegisterAdmin)

        // Get role from intent (passed from the previous activity)
        role = intent.getStringExtra("role")

        // Handle profile image selection
        val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            selectedImageUri = uri
            ivProfileImage.setImageURI(selectedImageUri)
        }

        txtSelectProfileImage.setOnClickListener {
            imagePicker.launch("image/*")
        }

        // Register button listener
        btnRegisterAdmin.setOnClickListener {
            registerAdmin()
        }
    }

    // Registration function
    private fun registerAdmin() {
        val businessName = txtBusinessName.text.toString()
        val firstName = txtAdminFirstName.text.toString()
        val lastName = txtAdminLastName.text.toString()
        val email = txtAdminEmail.text.toString()
        val password = txtAdminPassword.text.toString()

        // Validate fields
        if (email.isEmpty() || password.isEmpty() || selectedImageUri == null || businessName.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show()
            return
        }

        // Register user with Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid ?: ""
                uploadProfileImage(userId, businessName, firstName, lastName, email)
            } else {
                // Registration failed
                Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                // Prompt user to re-enter details
            }
        }
    }

    // Function to upload profile image to Firebase Storage
    private fun uploadProfileImage(userId: String, businessName: String, firstName: String, lastName: String, email: String) {
        val storageRef = storage.reference.child("profile_images/$userId.jpg")

        selectedImageUri?.let { uri ->
            storageRef.putFile(uri).addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                    saveAdminInfo(userId, businessName, firstName, lastName, email, imageUrl.toString())
                }.addOnFailureListener {
                    Toast.makeText(this, "Error retrieving image URL: ${it.message}", Toast.LENGTH_SHORT).show()
                    // User stays on the same page in case of failure
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Error uploading image: ${it.message}", Toast.LENGTH_SHORT).show()
                // User stays on the same page in case of failure
            }
        }
    }

    // Function to save admin info to Firebase Realtime Database
    private fun saveAdminInfo(userId: String, businessName: String, firstName: String, lastName: String, email: String, profileImageUrl: String) {
        val userInfo = mapOf(
            "businessName" to businessName,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "role" to role,
            "profileImage" to profileImageUrl
        )

        database.getReference("Users").child(userId).child("Details").setValue(userInfo)
            .addOnSuccessListener {
                Toast.makeText(this, "Admin registered successfully!", Toast.LENGTH_SHORT).show()
                navigateToMainActivity()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving admin info: ${e.message}", Toast.LENGTH_SHORT).show()
                // User stays on the same page in case of failure
            }
    }

    // Function to navigate to MainActivity
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("user_role", role)
        startActivity(intent)
        finish()
    }
}
