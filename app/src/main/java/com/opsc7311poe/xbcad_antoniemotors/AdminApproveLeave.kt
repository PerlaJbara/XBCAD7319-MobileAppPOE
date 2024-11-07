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


class AdminApproveLeaveFragment : Fragment() {

    private lateinit var leaveRequestContainer: LinearLayout
    private lateinit var businessID: String
    private val database = Firebase.database
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_approve_leave, container, false)
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

                    // Add the leave request to the UI
                    addLeaveRequestToLayout(employeeId ?: "", leaveType, startDate, endDate, businessId)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to fetch leave requests", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Add leave request to the UI for admin to approve
    private fun addLeaveRequestToLayout(employeeId: String, leaveType: String, startDate: String, endDate: String, businessId: String) {
        val leaveRequestLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        val employeeTextView = TextView(requireContext()).apply {
            text = "Employee ID: $employeeId"
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
                approveLeaveRequest(businessId, employeeId)
            }
        }

        leaveRequestLayout.addView(employeeTextView)
        leaveRequestLayout.addView(leaveTypeTextView)
        leaveRequestLayout.addView(leaveDatesTextView)
        leaveRequestLayout.addView(approveButton)

        leaveRequestContainer.addView(leaveRequestLayout)
    }

    // Function to approve leave request and update status
    private fun approveLeaveRequest(businessId: String, employeeId: String) {
        val leaveRef = database.reference.child("Users").child(businessId).child("Leave").child(employeeId).child("Status")
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

