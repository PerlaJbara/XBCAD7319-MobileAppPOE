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
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class AssignEmployeeTask : Fragment() {

    private lateinit var txtTaskName: TextView
    private lateinit var txtTaskDesc: TextView
    private lateinit var spVehicleChoice: Spinner
    private lateinit var spEmpChoice: Spinner
    private lateinit var txtSelectDueDate: TextView
    //private lateinit var swTaskApproval: Switch
    private lateinit var btnAssignTask: Button
    private lateinit var ivBackButton: ImageView


    private lateinit var businessId: String

    private var selectedEmployeeID: String = ""
    private var selectedServiceID: String = ""
    private var fetchedVehicleID: String? = ""

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
        //swTaskApproval = view.findViewById(R.id.switchTaskApproval)

        //loading spinners
        loadEmployees()
        loadServices()


        ivBackButton = view.findViewById(R.id.ivBackButton)
        //handling back button
        ivBackButton.setOnClickListener {
            replaceFragment(AdminTasksMenuFragment())
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

    private fun loadServices() {
        // Get the current logged-in user ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Query the database for employees under the current user's business ID
        val empRef = FirebaseDatabase.getInstance().getReference("Users/$businessId").child("Services")

        empRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val serviceMap = mutableMapOf<String, String>()
                val serviceNames = mutableListOf<String>()

                // Add a default blank selection at the start
                serviceNames.add("")

                // Check if there are employees associated with the user
                if (!snapshot.exists()) {
                    Toast.makeText(requireContext(), "No services found", Toast.LENGTH_SHORT).show()
                    return
                }

                // Loop through all employees associated with this user
                for (empSnapshot in snapshot.children) {
                    val serviceId = empSnapshot.key // Get service ID
                    val name = empSnapshot.child("name").getValue(String::class.java)

                    // Only add employees with the role "employee" and valid name details
                    if ( !name.isNullOrEmpty() && serviceId != null) {
                        serviceMap[serviceId] = name
                        serviceNames.add(name) // Add full name to spinner options
                    }
                }

                // Check if the list is empty (after filtering by role)
                if (serviceNames.size == 1) { // Only the blank option exists
                    Toast.makeText(requireContext(), "No services found", Toast.LENGTH_SHORT).show()
                    return
                }

                // Set up the spinner with the list of employee names
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, serviceNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spVehicleChoice.adapter = adapter

                // Handle employee selection from the spinner
                spVehicleChoice.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        val selectedServiceName = parent.getItemAtPosition(position).toString()
                        selectedServiceID = serviceMap.filterValues { it == selectedServiceName }.keys.firstOrNull().orEmpty()
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


    private fun loadEmployees() {
        // Get the current logged-in user ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Query the database for employees under the current user's business ID
        val empRef = FirebaseDatabase.getInstance().getReference("Users/$businessId").child("Employees")

        empRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val employeeMap = mutableMapOf<String, String>()
                val employeeNames = mutableListOf<String>()

                // Add a default blank selection at the start
                employeeNames.add("")

                // Check if there are employees associated with the user
                if (!snapshot.exists()) {
                    Toast.makeText(requireContext(), "No employees found", Toast.LENGTH_SHORT).show()
                    return
                }

                // Loop through all employees associated with this user
                for (empSnapshot in snapshot.children) {
                    val employeeId = empSnapshot.key // Get employee ID
                    val firstName = empSnapshot.child("firstName").getValue(String::class.java)
                    val lastName = empSnapshot.child("lastName").getValue(String::class.java)
                    val role = empSnapshot.child("role").getValue(String::class.java)

                    // Only add employees with the role "employee" and valid name details
                    if (role == "employee" && !firstName.isNullOrEmpty() && !lastName.isNullOrEmpty() && employeeId != null) {
                        val fullName = "$firstName $lastName"
                        employeeMap[employeeId] = fullName
                        employeeNames.add(fullName) // Add full name to spinner options
                    }
                }

                // Check if the list is empty (after filtering by role)
                if (employeeNames.size == 1) { // Only the blank option exists
                    Toast.makeText(requireContext(), "No employees with the role 'employee' found", Toast.LENGTH_SHORT).show()
                    return
                }

                // Set up the spinner with the list of employee names
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, employeeNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spEmpChoice.adapter = adapter

                // Handle employee selection from the spinner
                spEmpChoice.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        val selectedEmployeeName = parent.getItemAtPosition(position).toString()
                        selectedEmployeeID = employeeMap.filterValues { it == selectedEmployeeName }.keys.firstOrNull().orEmpty()
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


        // Converting date text to date values
        val dateTimeFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateTimeString = txtSelectDueDate.text.toString()

        // Attempt to parse due date
        val dueDate: Long? = try {
            dateTimeFormatter.parse(dateTimeString)?.time
        } catch (e: Exception) {
            null
        }


        //assigning data to temp empTask object
        empTaskToSubmit.apply {
            taskDescription = txtTaskDesc.text.toString()
            taskName = txtTaskName.text.toString()
            employeeID = selectedEmployeeID
            adminID = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            this.dueDate = dueDate
            Log.d("AssignEmployeeTask", "Due Date Entered: $dueDate")
            //taskApprovalRequired = swTaskApproval.isChecked
            //status by default "Not started"
            status = "Not Started"

            // If a vehicle/service is selected, assign vehicle to object
            if (spVehicleChoice.selectedItem.toString().isNotBlank()) {
                serviceID = selectedServiceID
                fetchVehicleID(serviceID!!)
                vehicleID = fetchedVehicleID
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

    private fun fetchVehicleID(serviceId: String) {
        val serviceRef = Firebase.database.reference.child("Users/$businessId/Services").child(serviceId).child("vehicleId")
        serviceRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(serviceSnapshot: DataSnapshot) {
                fetchedVehicleID = serviceSnapshot.getValue(String::class.java)
                if (fetchedVehicleID.isNullOrEmpty()) {
                    Log.e("fetchVehicleID", "No vehicleID found for service: $serviceId")
                } else {
                    Log.d("fetchVehicleID", "Fetched vehicleID: $fetchedVehicleID")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("fetchVehicleID", "Error fetching vehicleID: ${error.message}")
                fetchedVehicleID = null
            }
        })
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