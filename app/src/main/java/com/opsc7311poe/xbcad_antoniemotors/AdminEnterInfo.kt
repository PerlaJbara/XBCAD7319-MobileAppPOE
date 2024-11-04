package com.opsc7311poe.xbcad_antoniemotors

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.bumptech.glide.Glide
import android.widget.ProgressBar

class AdminEnterInfo : AppCompatActivity() {

    private lateinit var txtBusinessName: EditText
    private lateinit var txtAdminFirstName: EditText
    private lateinit var txtAdminLastName: EditText
    private lateinit var txtAdminEmail: EditText
    private lateinit var txtAdminPassword: EditText

    private lateinit var ivProfileImage: ImageView
    private lateinit var txtSelectProfileImage: TextView
    private lateinit var txtAdminPhone: EditText
    private lateinit var txtAdminAddress: EditText
    private var selectedImageUri: Uri? = null
    private lateinit var btnRegisterAdmin: Button
    private lateinit var progressBar: ProgressBar

    // Firebase instances
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            displayImage(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_enter_info)

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
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
        txtAdminAddress = findViewById(R.id.txtAdminPAddress)
        txtAdminPhone = findViewById(R.id.txtAdminPhone)
        progressBar = findViewById(R.id.progressBar)

        // Set onClickListener for image selection
        txtSelectProfileImage.setOnClickListener {
            // Launch the image picker
            pickImageLauncher.launch("image/*")
        }

        // Retrieve the business name from the previous intent
        val businessName = intent.getStringExtra("selectedBusinessName")
        txtBusinessName.setText(businessName)

        // Button click listener to register admin
        btnRegisterAdmin.setOnClickListener {
            registerAdmin()
        }
    }

    private fun displayImage(uri: Uri) {
        // Use Glide to load the selected image into the ImageView
        Glide.with(this)
            .load(uri)
            .into(ivProfileImage)
    }

    private fun registerAdmin() {
        val businessName = txtBusinessName.text.toString()
        val firstName = txtAdminFirstName.text.toString()
        val lastName = txtAdminLastName.text.toString()
        val email = txtAdminEmail.text.toString()
        val password = txtAdminPassword.text.toString()
        val phone = txtAdminPhone.text.toString()
        val address = txtAdminAddress.text.toString()

        if (businessName.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Hide register button and show progress bar
        btnRegisterAdmin.visibility = Button.INVISIBLE
        progressBar.visibility = ProgressBar.VISIBLE

        // Create the admin account in Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid

                    // Retrieve the owner ID and business ID
                    retrieveOwnerIdAndBusinessId(businessName) { ownerId, businessId ->
                        // Save admin info to Firebase Database
                        saveAdminInfoToFirebase(userId, ownerId, businessId, businessName, firstName, lastName, email, phone, address)
                    }
                } else {
                    // Show register button and hide progress bar on failure
                    btnRegisterAdmin.visibility = Button.VISIBLE
                    progressBar.visibility = ProgressBar.GONE
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun retrieveOwnerIdAndBusinessId(businessName: String, callback: (String?, String?) -> Unit) {
        // Query the database to find the owner ID and business ID based on the business name
        database.child("Users").orderByChild("BusinessInfo/businessName").equalTo(businessName)
            .get()
            .addOnSuccessListener { snapshot ->
                for (userSnapshot in snapshot.children) {
                    val businessId = userSnapshot.key // Get the business ID
                    val employeeSnapshot = userSnapshot.child("Employees").children
                    for (employee in employeeSnapshot) {
                        val role = employee.child("role").getValue(String::class.java)
                        if (role == "owner") {
                            val ownerId = employee.key // Get the owner ID
                            callback(ownerId, businessId)
                            return@addOnSuccessListener
                        }
                    }
                }
                callback(null, null) // Return null if no owner or business found
            }
            .addOnFailureListener {
                callback(null, null) // Handle failure
            }
    }

    private fun saveAdminInfoToFirebase(userId: String?, ownerId: String?, businessId: String?, businessName: String, firstName: String, lastName: String, email: String, phone: String, address: String) {
        if (userId != null && ownerId != null && businessId != null) {
            val adminInfo = hashMapOf(
                "firstName" to firstName,
                "lastName" to lastName,
                "email" to email,
                "phone" to phone,
                "address" to address,
                "role" to "admin",
                "approval" to "pending",
                "companyName" to businessName,
                "businessID" to businessId, // Save business ID
                "managerID" to ownerId // Save owner ID (manager ID)
            )

            // Save the admin info under the appropriate business path in the Pending node
            database.child("Users").child(businessId).child("Pending").child(userId).setValue(adminInfo)
                .addOnSuccessListener {
                    Toast.makeText(this, "Admin registered successfully!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@AdminEnterInfo, SuccessActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    // Show register button and hide progress bar on failure
                    btnRegisterAdmin.visibility = Button.VISIBLE
                    progressBar.visibility = ProgressBar.GONE
                    Toast.makeText(this, "Failed to save admin info: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {

            btnRegisterAdmin.visibility = Button.VISIBLE
            progressBar.visibility = ProgressBar.GONE
            Toast.makeText(this, "Failed to retrieve owner ID or business ID", Toast.LENGTH_SHORT).show()
        }
    }
}
