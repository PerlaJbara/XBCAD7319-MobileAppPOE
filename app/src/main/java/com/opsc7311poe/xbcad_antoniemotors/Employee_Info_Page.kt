package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class Employee_Info_Page : Fragment() {

    private lateinit var btnSubmit: Button
    private lateinit var txtName: TextView
    private lateinit var txtSurname: TextView
    private lateinit var txtSal: TextView
    private lateinit var txtTotalLeave: TextView
    private lateinit var txtLeaveLeft: TextView
    private lateinit var txtNum: TextView
    private lateinit var txtEmail: TextView
    private lateinit var txtPasswordInput: TextView
    private lateinit var txtAddress: TextView
    private lateinit var btnBack: ImageView

    // Firebase instance
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register__employee__page, container, false)

        auth = FirebaseAuth.getInstance()

        btnSubmit = view.findViewById(R.id.btnregEmp)
        btnBack = view.findViewById(R.id.ivBackButton)

        btnSubmit.setOnClickListener {
            // Get the user input
            txtName = view.findViewById(R.id.txtEmpname)
            txtSurname = view.findViewById(R.id.txtEmpsurname)
            txtSal = view.findViewById(R.id.txtEmpsalary)
            txtTotalLeave = view.findViewById(R.id.txtTotal)
            txtLeaveLeft = view.findViewById(R.id.txtleft)
            txtNum = view.findViewById(R.id.txtnumber)
            txtEmail = view.findViewById(R.id.txtEmailInput)
            txtPasswordInput = view.findViewById(R.id.txtPasswordInput)
            txtAddress = view.findViewById(R.id.txtAddressInput)

            btnBack.setOnClickListener {
                replaceFragment(EmployeeFragment())
            }

            if (txtName.text.isBlank() || txtSurname.text.isBlank() || txtSal.text.isBlank() ||
                txtTotalLeave.text.isBlank() || txtLeaveLeft.text.isBlank() || txtNum.text.isBlank() ||
                txtEmail.text.isBlank() || txtPasswordInput.text.isBlank() || txtAddress.text.isBlank()
            ) {
                Toast.makeText(
                    requireContext(),
                    "Please enter all employee information.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Register the employee with Firebase Auth
                registerEmployee()
            }
        }
        return view
    }

    private fun registerEmployee() {
        val email = txtEmail.text.toString()
        val password = txtPasswordInput.text.toString()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val employeeId = auth.currentUser?.uid ?: ""
                    val user = auth.currentUser

                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(txtName.text.toString())
                        .build()

                    user?.updateProfile(profileUpdates)

                    // Save employee info to Firebase Realtime Database
                    saveEmployeeToDatabase(employeeId)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Registration failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun saveEmployeeToDatabase(employeeId: String) {
        val database = Firebase.database
        val userRef = database.getReference("Users").child(employeeId)

        val emp = Employee(
            name = txtName.text.toString(),
            surname = txtSurname.text.toString(),
            salary = txtSal.text.toString(),
            totalLeave = txtTotalLeave.text.toString(),
            leaveLeft = txtLeaveLeft.text.toString(),
            number = txtNum.text.toString(),
            email = txtEmail.text.toString(),
            password = txtPasswordInput.text.toString(),
            address = txtAddress.text.toString(),
            role = "employee", // Assign role as employee
            businessName = txtName.text.toString(), // Business name set to employee's name
            profileImage = null // Set profile image to null for now
        )

        userRef.setValue(emp)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Employee successfully added", Toast.LENGTH_LONG)
                    .show()
                replaceFragment(EmployeeFragment())
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Error adding employee: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun replaceFragment(fragment: Fragment) {
        Log.d("Register_Employee_Page", "Replacing fragment: ${fragment::class.java.simpleName}")
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }


    data class Employee(
        var name: String?,
        var surname: String?,
        var salary: String?,
        var totalLeave: String?,
        var leaveLeft: String?,
        var number: String?,
        var email: String?,
        var password: String?,
        var address: String?,
        var role: String?,  // Role set to employee
        var businessName: String?,  // Business name for employee
        var profileImage: String? = null // Set to null for now
    ) {

        constructor() : this(null, null, null, null, null, null, null, null, null, null, null)

    }
}