package com.opsc7311poe.xbcad_antoniemotors

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class EmpEnterInfo : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var txtEmpFirstName: EditText
    private lateinit var txtEmpLastName: EditText
    private lateinit var txtEmpEmail: EditText
    private lateinit var txtEmpPassword: EditText
    private lateinit var txtEmpPhone: EditText
    private lateinit var txtEmpPAddress: EditText
    private lateinit var btnRegisterEmp: Button
    private lateinit var ivEmpProfPicture: ImageView
    private lateinit var opIn: Switch
    private lateinit var progressBar: ProgressBar

    private var businessId: String? = null
    private var managerId: String? = null
    private var role: String = "employee"
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emp_enter_info)

        auth = FirebaseAuth.getInstance()

        // Retrieve Intent data
        businessId = intent.getStringExtra("businessId")
        managerId = intent.getStringExtra("adminId") // Receiving admin ID as managerId
        role = intent.getStringExtra("role") ?: "employee"

        // Initialize views
        txtEmpFirstName = findViewById(R.id.txtEmpFirstName)
        txtEmpLastName = findViewById(R.id.txtEmpLastName)
        txtEmpEmail = findViewById(R.id.txtEmpEmail)
        txtEmpPassword = findViewById(R.id.txtEmpPassword)
        txtEmpPhone = findViewById(R.id.txtEmpPhone)
        txtEmpPAddress = findViewById(R.id.txtEmpPAddress)
        ivEmpProfPicture = findViewById(R.id.ivEmpProfPicture)
        btnRegisterEmp = findViewById(R.id.btnRegisterEmp)
        opIn = findViewById(R.id.opIn)
        progressBar = findViewById(R.id.progressBar)

        // ImageView click listener to open gallery
        ivEmpProfPicture.setOnClickListener {
            pickImage()
        }

        // Register button click listener
        btnRegisterEmp.setOnClickListener {
            registerEmployee()
        }
    }

    // Use GetContent launcher to pick an image
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            displayImage(uri)
        }
    }

    private fun pickImage() {
        pickImageLauncher.launch("image/*")
    }

    private fun displayImage(uri: Uri) {
        // Use Glide to load the selected image into the ImageView
        Glide.with(this)
            .load(uri)
            .into(ivEmpProfPicture)
    }

    private fun registerEmployee() {
        // Show progress bar while processing registration
        progressBar.visibility = ProgressBar.VISIBLE
        btnRegisterEmp.visibility = View.GONE

        // Check required fields
        if (businessId == null || managerId == null) {
            Toast.makeText(this, "Business ID or Manager ID is missing.", Toast.LENGTH_SHORT).show()
            progressBar.visibility = ProgressBar.GONE
            btnRegisterEmp.visibility = View.VISIBLE
            return
        }

        val firstName = txtEmpFirstName.text.toString().trim()
        val lastName = txtEmpLastName.text.toString().trim()
        val email = txtEmpEmail.text.toString().trim()
        val password = txtEmpPassword.text.toString().trim()
        val phone = txtEmpPhone.text.toString().trim()
        val address = txtEmpPAddress.text.toString().trim()
        val leaderboardOptIn = opIn.isChecked

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            progressBar.visibility = ProgressBar.GONE
            btnRegisterEmp.visibility = View.VISIBLE
            return
        }

        // Register the user with Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Upload the image if selected, otherwise proceed to save data
                    if (selectedImageUri != null) {
                        uploadImageToFirebaseStorage { imageUrl ->
                            saveEmployeeToDatabase(firstName, lastName, email, password, phone, address, leaderboardOptIn, imageUrl)
                        }
                    } else {
                        saveEmployeeToDatabase(firstName, lastName, email, password, phone, address, leaderboardOptIn, null)
                    }
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = ProgressBar.GONE
                    btnRegisterEmp.visibility = View.VISIBLE
                }
            }
    }

    private fun uploadImageToFirebaseStorage(callback: (String) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().getReference("employee_profile_images/${UUID.randomUUID()}.jpg")
        selectedImageUri?.let { uri ->
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        callback(downloadUri.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = ProgressBar.GONE
                    btnRegisterEmp.visibility = View.VISIBLE
                }
        } ?: run {
            Toast.makeText(this, "Image URI is null. Select an image.", Toast.LENGTH_SHORT).show()
            progressBar.visibility = ProgressBar.GONE
            btnRegisterEmp.visibility = View.VISIBLE
        }
    }

    private fun saveEmployeeToDatabase(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        phone: String,
        address: String,
        leaderboardOptIn: Boolean,
        imageUrl: String?
    ) {
        val empId = UUID.randomUUID().toString().replace("-", "")
        val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(businessId!!).child("Pending")

        val employeeData = hashMapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "password" to password,
            "phone" to phone,
            "address" to address,
            "role" to role,
            "businessID" to businessId,
            "managerID" to managerId,
            "approval" to "pending",
            "leaderboard" to leaderboardOptIn,
            "profileImageUrl" to imageUrl
        )

        dbRef.child(empId).setValue(employeeData).addOnCompleteListener { task ->
            progressBar.visibility = ProgressBar.GONE
            if (task.isSuccessful) {
                val intent = Intent(this@EmpEnterInfo, SuccessActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Failed to register employee. Please Try Again.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}