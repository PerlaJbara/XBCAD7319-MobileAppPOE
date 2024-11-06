package com.opsc7311poe.xbcad_antoniemotors

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.type.DateTime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class AssignEmployeeTask : Fragment() {

    private lateinit var txtTaskName: TextView
    private lateinit var txtTaskDesc: TextView
    private lateinit var spVehicleChoice: Spinner
    private lateinit var spEmpChoice: Spinner
    private lateinit var txtSelectDueDate: TextView
    private lateinit var swTaskApproval: Switch
    private lateinit var btnAssignTask: Button
    private lateinit var ivBackButton: ImageView


    private lateinit var businessId: String

    private var selectedEmployeeID: String = ""
    private var selectedVehicleId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_assign_employee_task, container, false)

        businessId = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).getString("business_id", null)!!

        //connecting elements
        txtTaskName = view.findViewById(R.id.txtTaskName)
        txtTaskDesc = view.findViewById(R.id.txtEmpTaskDescription)
        spVehicleChoice = view.findViewById(R.id.spSelectVehicle)
        spEmpChoice = view.findViewById(R.id.spSelectEmp)
        txtSelectDueDate = view.findViewById(R.id.txtSelectTaskDueDate)
        swTaskApproval = view.findViewById(R.id.switchTaskApproval)

        //loading spinners
        loadEmployees()
        loadVehicles()


        ivBackButton = view.findViewById(R.id.ivBackButton)
        //handling back button
        ivBackButton.setOnClickListener {
            replaceFragment(AdminEmpFragment())
        }

        //handling assigning task

        btnAssignTask = view.findViewById(R.id.btnAssignTask)
        btnAssignTask.setOnClickListener {

            submitTask()
            //going back to task menu
            it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            replaceFragment(AdminTasksMenuFragment())
        }

        //allowing user to pick date
        txtSelectDueDate.setOnClickListener{
            pickDateTime(txtSelectDueDate)
        }




        return view
    }

    private fun loadVehicles() {
        // Create a list with a single blank option
        val vehicleOptions = listOf("")

        // Set up the adapter with the blank option
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, vehicleOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        spVehicleChoice.adapter = adapter
    }


    private fun loadEmployees() {
        // Get the current logged-in user ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Query the database for customers directly under the current user's ID
        val empRef = FirebaseDatabase.getInstance().getReference("Users/$businessId").child("Employees")

        empRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val customerMap = mutableMapOf<String, String>()
                val customerNames = mutableListOf<String>()

                // Add a default blank selection
                customerNames.add("")

                // Check if there are customers associated with the user
                if (!snapshot.exists()) {
                    Toast.makeText(requireContext(), "No employees found ", Toast.LENGTH_SHORT).show()
                    return
                }

                // Loop through all customers associated with this user
                for (empSnapshot in snapshot.children) {
                    val customerId = empSnapshot.key // Get customer ID
                    val firstName = empSnapshot.child("firstName").getValue(String::class.java)
                    val lastName = empSnapshot.child("lastName").getValue(String::class.java)

                    // Log data to check if it's being fetched correctly
                    Log.d("AssignEmployeeTask", "Fetched Employee for spinner: $firstName $lastName, ID: $customerId")

                    // Only add customer if names are not null or empty
                    if (!firstName.isNullOrEmpty() && !lastName.isNullOrEmpty() && customerId != null) {
                        val fullName = "$firstName $lastName"
                        customerMap[customerId] = fullName
                        customerNames.add(fullName) // Add full name to spinner options
                    }
                }

                // Check if the list is empty
                if (customerNames.isEmpty()) {
                    Toast.makeText(requireContext(), "No customers found", Toast.LENGTH_SHORT).show()
                    return
                }

                // Set up the spinner with the list of customer names
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, customerNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spEmpChoice.adapter = adapter

                // Handle customer selection from the spinner
                spEmpChoice.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        val selectedCustomerName = parent.getItemAtPosition(position).toString()
                        selectedEmployeeID = customerMap.filterValues { it == selectedCustomerName }.keys.firstOrNull().orEmpty()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // Handle case where nothing is selected if necessary
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error loading employee data: ${error.message}")
                Toast.makeText(requireContext(), "Error loading employee data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun submitTask() {

        var empTaskToSubmit: EmpTask = EmpTask()

        //checking compulsory stuff is filled
        if (txtTaskDesc.text.isBlank()) {
            Toast.makeText(requireContext(), "Please fill in the task description.", Toast.LENGTH_SHORT).show()
            return
        }

        if (txtTaskName.text.isBlank()) {
            Toast.makeText(requireContext(), "Please fill in the task name.", Toast.LENGTH_SHORT).show()
            return
        }

        if (spEmpChoice.selectedItem.toString().isBlank()) {
            Toast.makeText(requireContext(), "Please select an employee.", Toast.LENGTH_SHORT).show()
            return
        }


        //converting date texts to date values
        val dateTimeFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateTimeString = txtSelectDueDate.text.toString()
        val dueDate: Long? = dateTimeFormatter.parse(dateTimeString)?.time

        if (dueDate == null) {
            Toast.makeText(requireContext(), "Please select a valid due date and time.", Toast.LENGTH_SHORT).show()
            return
        }


        //assigning data to temp empTask object
        empTaskToSubmit.apply {
            taskDescription = txtTaskDesc.text.toString()
            taskName = txtTaskName.text.toString()
            employeeID = selectedEmployeeID
            adminID = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            this.dueDate = dueDate
            taskApprovalRequired = swTaskApproval.isChecked
            //status by default "Not started"
            status = "Not Started"

            // If a vehicle is selected, assign vehicle to object
            if (spVehicleChoice.selectedItem.toString().isNotBlank()) {
                vehicleNumberPlate = selectedVehicleId
            }}


        val database = Firebase.database
        val taskRef = database.getReference("Users/$businessId").child("EmployeeTasks")

        //pushing task to firebase
        taskRef.push().setValue(empTaskToSubmit)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Employee Task successfully added", Toast.LENGTH_LONG).show()

            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "An error occurred while adding an employee task:" + it.toString() , Toast.LENGTH_LONG).show()
            }



    }


    fun pickDateTime(edittxt: TextView) {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        // Date Picker Dialog
        val datePickDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Format the selected date
                val formattedDay = String.format("%02d", selectedDay)
                val formattedMonth = String.format("%02d", selectedMonth + 1)
                val selectedDate = "$formattedDay/$formattedMonth/$selectedYear"

                // After picking date, show time picker
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                val minute = cal.get(Calendar.MINUTE)

                // Time Picker Dialog
                val timePickDialog = TimePickerDialog(
                    requireContext(),
                    { _, selectedHour, selectedMinute ->
                        // Format the selected time
                        val formattedHour = String.format("%02d", selectedHour)
                        val formattedMinute = String.format("%02d", selectedMinute)
                        val selectedTime = "$formattedHour:$formattedMinute"

                        // Display date and time in TextView
                        edittxt.text = "$selectedDate $selectedTime"
                    }, hour, minute, true
                )
                timePickDialog.show()
            }, year, month, day
        )

        datePickDialog.show()
    }


    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }


}