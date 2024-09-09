package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.opsc7311poe.timewize_progpoe.CustomerData

class AddCustomerFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var nameField: EditText
    private lateinit var surnameField: EditText
    private lateinit var mobileNumField: EditText
    private lateinit var emailField: EditText
    private lateinit var addressField: EditText
    private lateinit var submitButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_customer, container, false)

        // Initializing Firebase Database
        database = FirebaseDatabase.getInstance().reference.child("Customers")

        // Initialize Views
        nameField = view.findViewById(R.id.edttxtRegName)
        surnameField = view.findViewById(R.id.edttxtRegSurname)
        mobileNumField = view.findViewById(R.id.edttxtRegMobNumber)
        emailField = view.findViewById(R.id.edttxtEmail)
        addressField = view.findViewById(R.id.edttxtAddress)
        submitButton = view.findViewById(R.id.btnSubmitRegCustomer)

        // Set onClickListener for the submit button
        submitButton.setOnClickListener {
            addCustomer()
        }

        return view
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

        // Create a new customer object
        val customer = CustomerData(name, surname, mobileNum, email, address)

        // Add customer to Firebase
        database.push().setValue(customer).addOnCompleteListener { task ->
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
}

