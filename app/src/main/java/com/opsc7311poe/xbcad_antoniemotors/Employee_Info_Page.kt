package com.opsc7311poe.xbcad_antoniemotors

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class Employee_Info_Page : Fragment() {

    lateinit var txtName: TextView
    lateinit var txtNum: TextView
    lateinit var txtEmail: TextView
    lateinit var txtAddress: TextView
    lateinit var txtSal: TextView

    private lateinit var btnBack: ImageView
    private lateinit var btnDeleteEmployee: Button
    private lateinit var btnEditEmp: Button
    private lateinit var btnSaveup: Button


    // New fields for editing
    lateinit var txtnewNum: EditText
    lateinit var txtnewEmail: EditText
    lateinit var txtnewAddress: EditText
    lateinit var txtnewSal: EditText

    private lateinit var businessID: String

    var isEditing = false
    var employeeName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_employee__info__page, container, false)

        businessID = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).getString("business_id", null)!!
        employeeName = arguments?.getString("employeeId")

        // Initialize views
        txtName = view.findViewById(R.id.txtheaderempname)
        txtNum = view.findViewById(R.id.txtempcell_view)
        txtEmail = view.findViewById(R.id.txtempEmail_view)
        txtAddress = view.findViewById(R.id.txtempAddress_view)
        txtSal = view.findViewById(R.id.txtmonthly_view)

        btnBack = view.findViewById(R.id.ivBackButton)
        btnDeleteEmployee = view.findViewById(R.id.btnDeleteEmp)
        btnEditEmp = view.findViewById(R.id.btnEditEmployeeInfo)
        btnSaveup = view.findViewById(R.id.btnSaveEmployeeInfo)

        // New fields for editing
        txtnewNum = view.findViewById(R.id.txtupdatecell)
        txtnewEmail = view.findViewById(R.id.txtupdateEmail)
        txtnewAddress = view.findViewById(R.id.txtupdateAddress)
        txtnewSal = view.findViewById(R.id.txtupdateMonthly)

        displayDetails()

        // Setup listeners
        btnBack.setOnClickListener {
            replaceFragment(EmployeeFragment())
        }

        btnEditEmp.setOnClickListener {
            toggleEditing(true)
        }

        btnSaveup.setOnClickListener {
            saveEmployeeData(employeeName)
        }

        // Delete employee
        btnDeleteEmployee.setOnClickListener {
            // Confirm employeeId is available
            employeeName?.let { empId ->
                deleteEmployee(empId)
            }
        }

        // Fetch and display employee data
        if (employeeName != null) {
            displayDetails()
        }

        return view
    }

    private fun displayDetails() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null && employeeName != null) {
            val database = Firebase.database
            val empRef = database.getReference("Users").child(businessID).child("Employees")

            // Assuming employeeName is the unique ID of the employee
            // Use this unique ID directly to fetch the employee details
            if (employeeName != null) {
                fetchEmployeeDetails(employeeName!!)
            }
        }
    }

    private fun fetchEmployeeDetails(employeeId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val database = Firebase.database
            val empRef = database.getReference("Users").child(businessID).child("Employees")

            // Directly fetch employee details using the employee's unique ID
            val employeeRef = empRef.child(employeeId)

            employeeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val fetchedEmp = dataSnapshot.getValue(EmployeeInfo::class.java)
                    if (fetchedEmp != null) {
                        // Populate the UI with employee data
                        updateEmployeeUI(fetchedEmp)
                    } else {
                        Toast.makeText(requireContext(), "Employee not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(requireContext(), "Error fetching employee data: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun updateEmployeeUI(employee: EmployeeInfo?) {
        if (employee != null) {
            txtName.text = "${employee.firstName} ${employee.lastName}"
            txtNum.text = employee.phone // This should show the correct phone number
            txtEmail.text = employee.email
            txtAddress.text = employee.address
            txtSal.text = employee.salary // This should show the correct salary

            // Fill in the editable fields with existing employee info
            txtnewNum.setText(employee.phone)
            txtnewEmail.setText(employee.email)
            txtnewAddress.setText(employee.address)
            txtnewSal.setText(employee.salary)
        }
    }

    private fun saveEmployeeData(employeeName: String?) {
        val newNumber = txtnewNum.text.toString()
        val newEmail = txtnewEmail.text.toString()
        val newAddress = txtnewAddress.text.toString()
        val newSalary = txtnewSal.text.toString()

        if (newNumber.isNotEmpty() && newEmail.isNotEmpty() && newAddress.isNotEmpty() && newSalary.isNotEmpty()) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null && employeeName != null) {
                val database = Firebase.database
                val employeeRef = database.getReference("Users").child(businessID).child("Employees").child(employeeName)

                val updates = mapOf<String, Any>(
                    "phone" to newNumber,
                    "email" to newEmail,
                    "address" to newAddress,
                    "salary" to newSalary
                )

                employeeRef.updateChildren(updates).addOnSuccessListener {
                    Toast.makeText(requireContext(), "Employee updated successfully", Toast.LENGTH_SHORT).show()
                    toggleEditing(false)
                }.addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error updating employee: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleEditing(isEditing: Boolean) {
        this.isEditing = isEditing
        // Toggle visibility of UI elements based on editing mode
        if (isEditing) {
            txtName.visibility = View.GONE
            txtNum.visibility = View.GONE
            txtEmail.visibility = View.GONE
            txtAddress.visibility = View.GONE
            txtSal.visibility = View.GONE

            txtnewNum.visibility = View.VISIBLE
            txtnewEmail.visibility = View.VISIBLE
            txtnewAddress.visibility = View.VISIBLE
            txtnewSal.visibility = View.VISIBLE
            btnSaveup.visibility = View.VISIBLE
        } else {
            txtName.visibility = View.VISIBLE
            txtNum.visibility = View.VISIBLE
            txtEmail.visibility = View.VISIBLE
            txtAddress.visibility = View.VISIBLE
            txtSal.visibility = View.VISIBLE

            txtnewNum.visibility = View.GONE
            txtnewEmail.visibility = View.GONE
            txtnewAddress.visibility = View.GONE
            txtnewSal.visibility = View.GONE
            btnSaveup.visibility = View.GONE
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.fragment_container, fragment)?.commit()
    }

    private fun deleteEmployee(employeeId: String) {
        val auth = FirebaseAuth.getInstance()
        val database = Firebase.database

        val user = auth.currentUser

        // Ensure the employeeId matches the currently authenticated user
        if (user != null && user.uid == employeeId) {
            // Delete the Firebase Authentication account first
            user.delete().addOnCompleteListener { deleteAuthTask ->
                if (deleteAuthTask.isSuccessful) {
                    // If authentication deletion is successful, proceed to delete employee data from the database
                    val employeeRef = database.getReference("Users").child(businessID).child("Employees").child(employeeId)

                    employeeRef.removeValue().addOnSuccessListener {
                        // Both authentication and database deletion were successful
                        Toast.makeText(requireContext(), "Employee and authentication deleted successfully.", Toast.LENGTH_SHORT).show()

                        // Navigate back to EmployeeFragment after deletion
                        replaceFragment(EmployeeFragment())
                    }.addOnFailureListener { dbError ->
                        // Failed to delete employee data from the database
                        Toast.makeText(requireContext(), "Failed to delete employee data: ${dbError.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Failed to delete the authentication account
                    Toast.makeText(requireContext(), "Failed to delete employee authentication: ${deleteAuthTask.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Employee's UID does not match the authenticated user or no user is logged in
            Toast.makeText(requireContext(), "Employee authentication not found or mismatched.", Toast.LENGTH_SHORT).show()
        }
    }

}





















//    private fun fetchEmployeeDetails(firstName: String, lastName: String) {
//        val userId = FirebaseAuth.getInstance().currentUser?.uid
//        if (userId != null) {
//            val database = Firebase.database
//            val empRef = database.getReference("Users").child(businessID).child("Employees")
//
//            // Query to get employee details based on both first name and last name
//            val query = empRef.orderByChild("firstName").equalTo(firstName)
//
//            // Perform the query and listen for changes
//            query.addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(dataSnapshot: DataSnapshot) {
//                    for (projectSnapshot in dataSnapshot.children) {
//                        val fetchedEmp = projectSnapshot.getValue(EmployeeInfo::class.java)
//                        if (fetchedEmp?.lastName == lastName) {
//                            // If the last name matches, populate the UI with employee data
//                            updateEmployeeUI(fetchedEmp)
//                        }
//                    }
//                }
//
//                override fun onCancelled(databaseError: DatabaseError) {
//                    Toast.makeText(requireContext(), "Error fetching employee data: ${databaseError.message}", Toast.LENGTH_SHORT).show()
//                }
//            })
//        }
//    }
//    private fun updateEmployeeUI(employee: EmployeeInfo) {
//        txtName.text = "${employee.firstName} ${employee.lastName}"  // Concatenate first and last names
//        txtNum.text = employee.number
//        txtEmail.text = employee.email
//        txtAddress.text = employee.address
//        txtSal.text = employee.salary
//
//        // Fill in the editable fields with existing employee info
//        txtnewNum.setText(employee.number)
//        txtnewEmail.setText(employee.email)
//        txtnewAddress.setText(employee.address)
//        txtnewSal.setText(employee.salary)
//    }