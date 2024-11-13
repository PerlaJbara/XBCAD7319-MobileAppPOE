package com.opsc7311poe.xbcad_antoniemotors

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class EmpLeaveFragment : Fragment() {

    private lateinit var spinleave: Spinner
    private lateinit var txtremainleave: TextView
    private lateinit var txtleavestart: TextView
    private lateinit var txtleaveend: TextView
    private lateinit var btnrequestleave: Button
    private lateinit var txtRules: TextView
    private lateinit var btnback : Button

    private val database = Firebase.database
    private val auth = FirebaseAuth.getInstance()
    private lateinit var businessID: String
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_emp_leave, container, false)

        // Retrieve the business ID from shared preferences
        businessID = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).getString("business_id", null)!!

        // Initialize views
        spinleave = view.findViewById(R.id.spinnerLeaveType)
        txtremainleave = view.findViewById(R.id.txtLeaveRemaining)
        txtleavestart = view.findViewById(R.id.txtselleavestart)
        txtleaveend = view.findViewById(R.id.txtselleaveend)
        btnrequestleave = view.findViewById(R.id.btnRequest)
        txtRules = view.findViewById(R.id.txtLeaveRules)
        btnback = view.findViewById(R.id.ivBackButton)
        txtRules.visibility = View.GONE // Initially hidden

        // Set up spinner adapter with leave types
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.leave_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinleave.adapter = adapter
        }

        // Set listener for spinner selection
        spinleave.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedLeaveType = parent.getItemAtPosition(position).toString()
                displayLeaveRules(selectedLeaveType)
                fetchRemainingLeaveDays(selectedLeaveType)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        setupDatePicker(txtleavestart)
        setupDatePicker(txtleaveend)

        // Handle button press for saving leave data
        btnrequestleave.setOnClickListener {
            submitLeaveRequest()
        }

        btnback.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
    }

    // Function to display leave rules for the selected leave type
    private fun displayLeaveRules(leaveType: String) {
        val rules = when (leaveType) {
            "Sick Leave" -> "Sick Leave is allowed up to 30 days per year. Please submit a sick note to your manager for two days or more."
            "Parental Leave" -> "Parental Leave is available for up to 10 days."
            "Annual Leave" -> "Annual Leave accumulates at 15 days per year."
            else -> "Please contact HR for more information."
        }
        txtRules.text = rules
        txtRules.visibility = View.VISIBLE
    }

    // Function to retrieve remaining leave days for the selected leave type
    // Function to retrieve remaining leave days for the selected leave type
    private fun fetchRemainingLeaveDays(leaveType: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val leaveDaysRef = database.reference.child("Users").child(businessID).child("Employees")
            .child(currentUserId).child("Leave").child(leaveType)

        leaveDaysRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Using GenericTypeIndicator to properly retrieve the HashMap
                val genericTypeIndicator = object : GenericTypeIndicator<Map<String, Any>>() {}
                val leaveDetails = snapshot.getValue(genericTypeIndicator)

                // Retrieve current remaining leave days, defaulting to 0 if not available
                val remainingDays = leaveDetails?.get("leaveDays")?.toString()?.toIntOrNull() ?: 0

                // Calculate the requested leave duration
                val startDate = txtleavestart.text.toString()
                val endDate = txtleaveend.text.toString()
                val requestedDuration = calculateLeaveDuration(startDate, endDate)

                // Update the remaining leave days
                val updatedRemainingDays = remainingDays - requestedDuration

                // Display the updated remaining leave days in the UI
                txtremainleave.text = updatedRemainingDays.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to retrieve remaining leave days", Toast.LENGTH_SHORT).show()
            }
        })
    }



    // Set up DatePickerDialog for a given EditText
    private fun setupDatePicker(editText: TextView) {
        editText.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                editText.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
            }, year, month, day).show()
        }
    }

    // Submit the leave request to Firebase
    private fun submitLeaveRequest() {
        val currentUserId = auth.currentUser?.uid ?: return
        val selectedLeaveType = spinleave.selectedItem.toString()
        val startDate = txtleavestart.text.toString()
        val endDate = txtleaveend.text.toString()
        val remainingDays = txtremainleave.text.toString().toIntOrNull() ?: 0

        // Fetch the user's manager ID and name for the leave request
        database.reference.child("Users").child(businessID).child("Employees")
            .child(currentUserId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val managerID = snapshot.child("managerID").value.toString()
                    val userName = snapshot.child("firstName").value.toString() + " " + snapshot.child("lastName").value.toString()

                    val requestDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val leaveRequestData = mapOf(
                        "managerID" to managerID,
                        "userID" to currentUserId,
                        "userName" to userName,
                        "leaveType" to selectedLeaveType,
                        "startDate" to startDate,
                        "endDate" to endDate,
                        "remainingDays" to remainingDays,
                        "duration" to calculateLeaveDuration(startDate, endDate),
                        "requestDate" to requestDate,
                        "status" to "pending"
                    )

                    val leaveRequestRef = database.reference.child("Users").child(businessID)
                        .child("Employees").child(currentUserId).child("PendingLeave").push()

                    leaveRequestRef.setValue(leaveRequestData)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(requireContext(), "Leave request submitted", Toast.LENGTH_SHORT).show()
                                clearInputs()
                            } else {
                                Toast.makeText(requireContext(), "Failed to submit leave request", Toast.LENGTH_SHORT).show()
                            }
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // Calculate the duration of leave in days
    private fun calculateLeaveDuration(startDate: String, endDate: String): Int {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return try {
            val start = format.parse(startDate)
            val end = format.parse(endDate)
            val difference = end.time - start.time
            (difference / (1000 * 60 * 60 * 24)).toInt()
        } catch (e: Exception) {
            0
        }
    }

    // Clear input fields after submitting a request
    private fun clearInputs() {
        txtleavestart.text  = ""
        txtleaveend.text = ""
        spinleave.setSelection(0)
        txtremainleave.text = ""
        txtRules.visibility = View.GONE
    }
}
