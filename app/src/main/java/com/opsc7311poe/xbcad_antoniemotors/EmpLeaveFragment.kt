package com.opsc7311poe.xbcad_antoniemotors

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class EmpLeaveFragment : Fragment() {

    private lateinit var spinleave: Spinner
    private lateinit var txtremainleave: TextView
    private lateinit var txtleavestart: EditText
    private lateinit var txtleaveend: EditText
    private lateinit var btnrequestleave: Button

    private val database = Firebase.database
    private val auth = FirebaseAuth.getInstance()
    private lateinit var businessID: String

    private lateinit var datePickerDialog: DatePickerDialog
    private val calendar = Calendar.getInstance()

    // Map of leave types to remaining leave days
    private val leaveDaysMap = mapOf(
        "SICK LEAVE" to 30,
        "ANNUAL LEAVE" to 20,
        "MATERNITY LEAVE" to 90,
        "FAMILY RESPONSIBILITY LEAVE" to 10
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_emp_leave, container, false)

        // Initialize views
        spinleave = view.findViewById(R.id.spinnerLeaveType)
        txtremainleave = view.findViewById(R.id.txtLeaveRemaining)
        txtleavestart = view.findViewById(R.id.txtselleavestart)
        txtleaveend = view.findViewById(R.id.txtselleaveend)
        btnrequestleave = view.findViewById(R.id.btnRequest)

        // Set up spinner adapter with leave types
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.leave_types, // Replace with your leave types array resource ID
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinleave.adapter = adapter
        }

        // Set a listener to update remaining leave days based on selected leave type
        spinleave.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedLeaveType = parent.getItemAtPosition(position).toString()
                val remainingDays = leaveDaysMap[selectedLeaveType] ?: 0
                txtremainleave.text = remainingDays.toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Optional: Handle case when no item is selected
            }
        }

        setupDatePicker(txtleavestart)
        setupDatePicker(txtleaveend)

        // Get the current user's BusinessID
        fetchEmployeeBusinessID()

        // Handle button press for saving leave data
        btnrequestleave.setOnClickListener {
            val currentUserId = auth.currentUser?.uid
            val selectedLeaveType = spinleave.selectedItem.toString()
            val startDate = txtleavestart.text.toString()
            val endDate = txtleaveend.text.toString()

            if (currentUserId != null && startDate.isNotEmpty() && endDate.isNotEmpty() && ::businessID.isInitialized) {
                saveLeaveRequest(currentUserId, selectedLeaveType, startDate, endDate)
            } else {
                Toast.makeText(requireContext(), "Please enter all leave details", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    // Function to set up DatePicker for the given EditText
    private fun setupDatePicker(editText: EditText) {
        editText.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                editText.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
            }, year, month, day)

            datePickerDialog.show()
        }
    }

    // Function to retrieve the BusinessID for the current employee
    private fun fetchEmployeeBusinessID() {
        val currentUserId = auth.currentUser?.uid ?: return
        val employeeRef = database.reference.child("Users")

        employeeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (businessSnapshot in snapshot.children) {
                    val employees = businessSnapshot.child("Employees")
                    if (employees.hasChild(currentUserId)) {
                        businessID = businessSnapshot.key.toString()
                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to retrieve business ID", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Function to save leave request data in Firebase
    private fun saveLeaveRequest(userId: String, leaveType: String, startDate: String, endDate: String) {
        if (!::businessID.isInitialized || businessID.isEmpty()) {
            Toast.makeText(requireContext(), "Business ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Reference to the leave data node in Firebase database
        val leaveRef = database.reference.child("Users")
            .child(businessID) // Place under the correct Business ID
            .child("Leave") // Create or reference the Leave node
            .child(userId) // Use Employee ID as the unique ID under Leave

        val leaveDetails = mapOf(
            "leaveType" to leaveType,
            "startDate" to startDate,
            "endDate" to endDate,
            "Status" to "pending"
        )

        // Save leave request details under business and user node in Firebase
        leaveRef.setValue(leaveDetails)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Leave request saved", Toast.LENGTH_SHORT).show()
                    clearInputs()
                } else {
                    Toast.makeText(requireContext(), "Failed to save leave request", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Function to clear input fields
    private fun clearInputs() {
        txtleavestart.text.clear()
        txtleaveend.text.clear()
        spinleave.setSelection(0)
    }
}