package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class AdminApprovesLeaves : Fragment() {
    private lateinit var leaveRequestContainer: LinearLayout
    private lateinit var businessID: String
    private val database = Firebase.database
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("LeaderboardFragment", "on create view loaded")

        val view = inflater.inflate(R.layout.fragment_admin_approves_leaves, container, false)
        leaveRequestContainer = view.findViewById(R.id.leaveRequestContainer)

        // Fetch the current logged-in admin's business ID
        fetchAdminBusinessID()

        return view
    }

    // Fetch the current logged-in admin's BusinessID
    private fun fetchAdminBusinessID() {
        val currentAdminId = auth.currentUser?.uid ?: return
        val employeeRef = database.reference.child("Users")

        employeeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (businessSnapshot in snapshot.children) {
                    val employees = businessSnapshot.child("Employees")
                    if (employees.hasChild(currentAdminId)) {
                        // The admin is associated with this business
                        businessID = businessSnapshot.key.toString()
                        // Now fetch pending leave requests for the business
                        fetchPendingLeaveRequests(businessID)
                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to retrieve business ID", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Fetch pending leave requests for the business
    private fun fetchPendingLeaveRequests(businessId: String) {
        val leaveRef = database.reference.child("Users").child(businessId).child("Leave")

        leaveRef.orderByChild("Status").equalTo("pending").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                leaveRequestContainer.removeAllViews()  // Clear previous leave requests

                // Iterate through the pending leave requests and display them
                for (leaveSnapshot in snapshot.children) {
                    val employeeId = leaveSnapshot.key
                    val leaveType = leaveSnapshot.child("leaveType").getValue(String::class.java) ?: "N/A"
                    val startDate = leaveSnapshot.child("startDate").getValue(String::class.java) ?: "N/A"
                    val endDate = leaveSnapshot.child("endDate").getValue(String::class.java) ?: "N/A"
                    val status = leaveSnapshot.child("Status").getValue(String::class.java) ?: "N/A"

                    // Fetch employee name and add the leave request to the UI
                    if (employeeId != null) {
                        fetchEmployeeName(employeeId, leaveType, startDate, endDate, businessId)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to fetch leave requests", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Fetch the full name of the employee
    private fun fetchEmployeeName(employeeId: String, leaveType: String, startDate: String, endDate: String, businessId: String) {
        val employeeRef = database.reference.child("Users").child(businessId).child("Employees").child(employeeId)

        employeeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Retrieve first and last name from the database
                val firstName = snapshot.child("firstName").getValue(String::class.java) ?: "Unknown"
                val lastName = snapshot.child("lastName").getValue(String::class.java) ?: "Unknown"

                // Concatenate first and last name to get full name
                val fullName = "$firstName $lastName"

                // Now that we have the employee's full name, add the leave request to the layout
                addLeaveRequestToLayout(fullName, leaveType, startDate, endDate, businessId)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to fetch employee name", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Add leave request to the UI for admin to approve
    private fun addLeaveRequestToLayout(employeeName: String, leaveType: String, startDate: String, endDate: String, businessId: String) {
        val leaveRequestLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        val employeeTextView = TextView(requireContext()).apply {
            text = "Employee Name: $employeeName"
            textSize = 16f
        }

        val leaveTypeTextView = TextView(requireContext()).apply {
            text = "Leave Type: $leaveType"
            textSize = 16f
        }

        val leaveDatesTextView = TextView(requireContext()).apply {
            text = "Start: $startDate - End: $endDate"
            textSize = 16f
        }

        val approveButton = Button(requireContext()).apply {
            text = "Approve"
            setOnClickListener {
                approveLeaveRequest(businessId, employeeName)
            }
        }

        leaveRequestLayout.addView(employeeTextView)
        leaveRequestLayout.addView(leaveTypeTextView)
        leaveRequestLayout.addView(leaveDatesTextView)
        leaveRequestLayout.addView(approveButton)

        leaveRequestContainer.addView(leaveRequestLayout)
    }

    // Function to approve leave request and update status
    private fun approveLeaveRequest(businessId: String, employeeName: String) {
        val leaveRef = database.reference.child("Users").child(businessId).child("Leave").child(employeeName).child("Status")
        leaveRef.setValue("approved")
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Leave request approved", Toast.LENGTH_SHORT).show()
                // Refresh the leave requests after approval
                fetchPendingLeaveRequests(businessId)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to approve leave request", Toast.LENGTH_SHORT).show()
            }
    }
}