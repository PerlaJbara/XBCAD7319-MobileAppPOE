package com.opsc7311poe.xbcad_antoniemotors

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.io.InputStream
import com.bumptech.glide.request.RequestOptions


class EmpProfileFragment : Fragment() {


    private lateinit var txtAppName: TextView
    private lateinit var txtAppSurname: TextView
    private lateinit var txtAppEmail: TextView
    private lateinit var txtAppPhone: TextView
    private lateinit var txtAppAddress: TextView
    private lateinit var txtAppRole: TextView
    private lateinit var txtUpdatePic: TextView
    private lateinit var ivEmpPic: ImageView


    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private val PICK_IMAGE_REQUEST = 71
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_emp_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ensure each findViewById call explicitly defines the type
        txtAppName = view.findViewById<TextView>(R.id.txtAppName)
        txtAppSurname = view.findViewById<TextView>(R.id.txtAppSurname)
        txtAppEmail = view.findViewById<TextView>(R.id.txtAppEmail)
        txtAppPhone = view.findViewById<TextView>(R.id.txtAppPhone)
        txtAppAddress = view.findViewById<TextView>(R.id.txtAppAddress)
        txtAppRole = view.findViewById<TextView>(R.id.txtAppRole)
        txtUpdatePic = view.findViewById<TextView>(R.id.txtUpdatePic)
        ivEmpPic = view.findViewById<ImageView>(R.id.ivEmpPic)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance()

        // Load Employee Data
        loadEmployeeData()

        // Profile picture upload button
        ivEmpPic.setOnClickListener {
            openImageChooser()
        }

        // Update profile picture text click
        txtUpdatePic.setOnClickListener {
            uploadProfilePicture()
        }
    }

    private fun loadEmployeeData() {
        val userId = auth.currentUser?.uid ?: return

        database.child("Users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var employeeFound = false

                for (businessSnapshot in snapshot.children) {
                    val employeesSnapshot = businessSnapshot.child("Employees")
                    if (employeesSnapshot.hasChild(userId)) {
                        // Employee found
                        val employeeSnapshot = employeesSnapshot.child(userId)
                        val firstName = employeeSnapshot.child("firstName").value.toString()
                        val lastName = employeeSnapshot.child("lastName").value.toString()
                        val email = employeeSnapshot.child("email").value.toString()
                        val phone = employeeSnapshot.child("phone").value.toString()
                        val address = employeeSnapshot.child("address").value.toString()
                        val role = employeeSnapshot.child("role").value.toString()
                        val profilePicUrl = employeeSnapshot.child("profileImageUrl").value.toString()

                        // Set data to UI
                        txtAppName.text = firstName
                        txtAppSurname.text = lastName
                        txtAppEmail.text = email
                        txtAppPhone.text = phone
                        txtAppAddress.text = address
                        txtAppRole.text = role

                        // Load Profile Picture in a Circle
                        if (profilePicUrl.isNotEmpty()) {
                            displayProfilePicture(profilePicUrl)
                        }

                        employeeFound = true
                        break
                    }
                }

                if (!employeeFound) {
                    Toast.makeText(context, "Employee data not found.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load profile data.", Toast.LENGTH_SHORT).show()
            }
        })
    }



    private fun uploadProfilePicture() {
        val userId = auth.currentUser?.uid ?: return
        imageUri?.let {
            // Locate the employee node dynamically
            database.child("Users").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var businessId: String? = null
                    var employeeFound = false

                    // Locate the business ID where the employee is stored
                    for (businessSnapshot in snapshot.children) {
                        val employeesSnapshot = businessSnapshot.child("Employees")
                        if (employeesSnapshot.hasChild(userId)) {
                            businessId = businessSnapshot.key
                            employeeFound = true
                            break
                        }
                    }

                    if (employeeFound && businessId != null) {
                        // Reference to upload profile picture
                        val ref = storage.reference.child("employee_profile_images/$userId.jpg")
                        ref.putFile(it)
                            .addOnSuccessListener {
                                ref.downloadUrl.addOnSuccessListener { uri ->

                                    database.child("Users").child(businessId)
                                        .child("Employees").child(userId)
                                        .child("profileImageUrl").setValue(uri.toString())
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Toast.makeText(context, "Profile picture updated", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(context, "Employee data not found.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to load data.", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun openImageChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            val inputStream: InputStream? = context?.contentResolver?.openInputStream(imageUri!!)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            ivEmpPic.setImageBitmap(bitmap) // Preview selected image
        }
    }

    private fun displayProfilePicture(profilePicUrl: String) {
        Glide.with(this)
            .load(profilePicUrl)
            .apply(RequestOptions.circleCropTransform())
            .placeholder(R.drawable.vector_myprofile)
            .error(R.drawable.vector_myprofile)
            .into(ivEmpPic)
    }

}
