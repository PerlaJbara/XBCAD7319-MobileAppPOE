package com.opsc7311poe.xbcad_antoniemotors

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class EMPLeaveListFragment : Fragment() {

    private lateinit var btnBackButton: ImageView
    private lateinit var leaveListContainer: LinearLayout
    private val database = FirebaseDatabase.getInstance().reference

    // Get businessID from shared preferences
    private lateinit var businessID: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnBackButton = view.findViewById(R.id.ivBackButton)
        leaveListContainer = view.findViewById(R.id.leaveListContainer)

        // Retrieve the business ID from shared preferences
        businessID = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            .getString("business_id", null)!!

        btnBackButton.setOnClickListener {
            replaceFragment(AdminLeaveMenuFragment()) // Replace with your actual fragment class
        }

        // Fetch and display active leave requests for the same business
        fetchActiveLeaveRequests()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_e_m_p_leave_list, container, false)
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    // Fetch active leave requests from the database
    private fun fetchActiveLeaveRequests() {
        val leaveRef = database.child("Users").child(businessID).child("Leave") // Fetch the "Leave" node for the businessID
        val todayDate = getCurrentDate() // Current date in dd/MM/yyyy format

        Log.d("EMPLeaveListFragment", "Current Date: $todayDate")  // Debugging log

        leaveRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                leaveListContainer.removeAllViews()  // Clear the view

                var activeLeaveFound = false  // Flag to check if any leave is displayed

                for (leaveSnapshot in snapshot.children) {
                    val employeeId = leaveSnapshot.key  // Employee ID in the Leave node
                    val status = leaveSnapshot.child("Status").getValue(String::class.java) ?: continue
                    val startDate = leaveSnapshot.child("startDate").getValue(String::class.java) ?: continue
                    val endDate = leaveSnapshot.child("endDate").getValue(String::class.java) ?: continue

                    // Only proceed if the leave status is approved
                    if (status == "approved" && employeeId != null) {
                        // Fetch the employee name and display the leave details
                        fetchEmployeeNameAndDisplayLeave(employeeId, startDate, endDate, todayDate)
                        activeLeaveFound = true
                    } else if (todayDate > endDate) {
                        // Optionally, remove leave if it has ended
                        leaveSnapshot.ref.removeValue()
                    }
                }

                // Show message if no active leave found
                if (!activeLeaveFound) {
                    Toast.makeText(requireContext(), "No active leave requests found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load leave requests", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Fetch employee name using the employee ID and then display the leave details
    private fun fetchEmployeeNameAndDisplayLeave(employeeId: String, startDate: String, endDate: String, todayDate: String) {
        val employeeRef = database.child("Users").child(businessID).child("Employees").child(employeeId)

        employeeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(employeeSnapshot: DataSnapshot) {
                val employeeName = employeeSnapshot.child("firstName").getValue(String::class.java) + " " +
                        employeeSnapshot.child("lastName").getValue(String::class.java)
                Log.d("EMPLeaveListFragment", "Employee Name: $employeeName, Start Date: $startDate, End Date: $endDate")

                // Only display the leave if it's currently active
                if (isLeaveActive(todayDate, startDate, endDate)) {
                    addLeaveToList(employeeName ?: "Unknown", startDate, endDate)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load employee details", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Add leave details to the UI
    private fun addLeaveToList(employeeName: String, startDate: String, endDate: String) {
        val leaveItem = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            Log.d("EMPLeaveListFragment", "Adding leave item: $employeeName, $startDate to $endDate")
        }

        val employeeTextView = TextView(requireContext()).apply {
            text = "Employee: $employeeName"
            textSize = 16f
        }

        val leaveDatesTextView = TextView(requireContext()).apply {
            text = "Leave Dates: $startDate to $endDate"
            textSize = 16f
        }

        leaveItem.addView(employeeTextView)
        leaveItem.addView(leaveDatesTextView)
        leaveListContainer.addView(leaveItem)
    }

    // Get the current date in "dd/MM/yyyy" format
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }

    // Check if today's date is within the leave's start and end date range
    private fun isLeaveActive(today: String, startDate: String, endDate: String): Boolean {
        return today >= startDate && today <= endDate
    }
}