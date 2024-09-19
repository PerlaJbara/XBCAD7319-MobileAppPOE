package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.opsc7311poe.xbcad_antoniemotors.CustomerData

class AddCustomerFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var nameField: EditText
    private lateinit var surnameField: EditText
    private lateinit var mobileNumField: EditText
    private lateinit var emailField: EditText
    private lateinit var addressField: EditText
    private lateinit var submitButton: Button
    private lateinit var btnBack: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_customer, container, false)

        // Initializing Firebase Database
        //database = FirebaseDatabase.getInstance().reference.child("Customers")

        // Initialize Firebase Database and Views
        database = FirebaseDatabase.getInstance().reference
        nameField = view.findViewById(R.id.edttxtRegName)
        surnameField = view.findViewById(R.id.edttxtRegSurname)
        mobileNumField = view.findViewById(R.id.edttxtRegMobNumber)
        emailField = view.findViewById(R.id.edttxtEmail)
        addressField = view.findViewById(R.id.edttxtAddress)
        submitButton = view.findViewById(R.id.btnSubmitRegCustomer)
        btnBack = view.findViewById(R.id.ivBackButton)

        // Set onClickListener for the submit button
        submitButton.setOnClickListener {
            addCustomer()
        }

        btnBack.setOnClickListener() {
            replaceFragment(HomeFragment())
        }

        return view
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }



    private fun addCustomer() {
        val name = nameField.text.toString().trim()
        val surname = surnameField.text.toString().trim()
        val mobileNum = mobileNumField.text.toString().trim()
        val email = emailField.text.toString().trim()
        val address = addressField.text.toString().trim()

        // Check if any of the fields are empty
        if (TextUtils.isEmpty(name)) {
            nameField.error = "Please enter a name"
            return
        }
        if (TextUtils.isEmpty(surname)) {
            surnameField.error = "Please enter a surname"
            return
        }
        if (TextUtils.isEmpty(mobileNum)) {
            mobileNumField.error = "Please enter a mobile number"
            return
        }
        if (TextUtils.isEmpty(email)) {
            emailField.error = "Please enter an email address"
            return
        }
        if (TextUtils.isEmpty(address)) {
            addressField.error = "Please enter a physical address"
            return
        }

        // Get the current logged-in user's UID from FirebaseAuth
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        // Ensure the user is logged in before proceeding
        if (currentUserUid != null) {
            // Generate a unique customer ID under the user's node
            val customerId = database.child(currentUserUid).child("Customers").push().key

            // Create a new customer object
            val customer = customerId?.let {
                CustomerData(
                    CustomerID = it,
                    CustomerName = name,
                    CustomerSurname = surname,
                    CustomerMobileNum = mobileNum,
                    CustomerEmail = email,
                    CustomerAddress = address
                )
            }

            // Saving the customer under the current user's UID in Firebase
            if (customerId != null) {
                database.child(currentUserUid).child("Customers").child(customerId)
                    .setValue(customer).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Customer added successfully", Toast.LENGTH_SHORT).show()

                            // Clear the input fields after successful submission
                            nameField.text.clear()
                            surnameField.text.clear()
                            mobileNumField.text.clear()
                            emailField.text.clear()
                            addressField.text.clear()
                        } else {
                            Toast.makeText(context, "Failed to add customer. Try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        } else {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }


}

