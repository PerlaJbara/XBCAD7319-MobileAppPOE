package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Locale

class AdminApprovesLeaves : Fragment() {
    private lateinit var leaveRequestContainer: LinearLayout
    private lateinit var businessID: String
    private val database = Firebase.database
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

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
                        // Now fetch leave requests for this business
                        fetchLeaveRequestsForBusiness(businessID, currentAdminId)
                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to retrieve business ID", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Fetch leave requests for the business where managerID matches the logged-in admin
    private fun fetchLeaveRequestsForBusiness(businessId: String, managerId: String) {
        val employeesRef = database.reference.child("Users").child(businessId).child("Employees")

        employeesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                leaveRequestContainer.removeAllViews()  // Clear previous leave requests

                // Loop through all employees
                for (employeeSnapshot in snapshot.children) {
                    val employeeId = employeeSnapshot.key
                    val managerID = employeeSnapshot.child("managerID").getValue(String::class.java)

                    // Check if the managerID matches the logged-in admin's ID
                    if (managerID == managerId) {
                        // Now fetch the pending leave requests for this employee
                        fetchPendingLeaveRequests(businessId, employeeId)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to fetch employees", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Fetch pending leave requests for the employee
    private fun fetchPendingLeaveRequests(businessId: String, employeeId: String?) {
        if (employeeId == null) return

        val leaveRef = database.reference.child("Users").child(businessId).child("Employees")
            .child(employeeId).child("PendingLeave")

        leaveRef.orderByChild("Status").equalTo("pending").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (leaveSnapshot in snapshot.children) {
                    val leaveType = leaveSnapshot.child("leaveType").getValue(String::class.java) ?: "N/A"
                    val startDate = leaveSnapshot.child("startDate").getValue(String::class.java) ?: "N/A"
                    val endDate = leaveSnapshot.child("endDate").getValue(String::class.java) ?: "N/A"

                    // Fetch employee name and add the leave request to the UI
                    fetchEmployeeName(employeeId, leaveType, startDate, endDate, businessId)
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
                addLeaveRequestToLayout(employeeId, fullName, leaveType, startDate, endDate, businessId)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to fetch employee name", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Add leave request to the UI for admin to approve
    private fun addLeaveRequestToLayout(employeeId: String, employeeName: String, leaveType: String, startDate: String, endDate: String, businessId: String) {
        // Inflate the card layout
        val cardView = layoutInflater.inflate(R.layout.card_leave_approval, leaveRequestContainer, false) as CardView

        // Set values for each field in the card layout
        val txtEmployeeName = cardView.findViewById<TextView>(R.id.txtEmployeeName)
        val txtLeaveType = cardView.findViewById<TextView>(R.id.txtLeaveType)
        val txtStartDate = cardView.findViewById<TextView>(R.id.txtStartDate)
        val txtEndDate = cardView.findViewById<TextView>(R.id.txtEndDate)
        val txtTotalDuration = cardView.findViewById<TextView>(R.id.txtTotalDuration)
        val btnViewMore = cardView.findViewById<Button>(R.id.btnRestoreTask)

        // Populate the text fields with data
        txtEmployeeName.text = employeeName
        txtLeaveType.text = leaveType
        txtStartDate.text = startDate
        txtEndDate.text = endDate

        // Calculate the total duration (in days)
        val totalDuration = calculateLeaveDuration(startDate, endDate)
        txtTotalDuration.text = "$totalDuration days"

        // Set button click listeners for approving and denying the leave request
        btnViewMore.setOnClickListener {
            // Example of action for the View More button, like showing more details in a new screen
            Toast.makeText(requireContext(), "Viewing more for $employeeName", Toast.LENGTH_SHORT).show()
        }

        // Add the card to the container
        leaveRequestContainer.addView(cardView)
    }

    // Helper function to calculate the total duration of leave
    private fun calculateLeaveDuration(startDate: String, endDate: String): Int {
        val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return try {
            val start = format.parse(startDate)
            val end = format.parse(endDate)
            val difference = end.time - start.time
            (difference / (1000 * 60 * 60 * 24)).toInt()
        } catch (e: Exception) {
            0
        }
    }
}
