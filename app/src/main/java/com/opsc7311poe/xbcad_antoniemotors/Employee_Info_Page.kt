package com.opsc7311poe.xbcad_antoniemotors

import android.app.DatePickerDialog
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Employee_Info_Page : Fragment() {

    lateinit var txtName: TextView
    lateinit var txtNum: TextView
    lateinit var txtEmail: TextView
    lateinit var txtAddress: TextView
    lateinit var txtRemainingLeave: TextView
    lateinit var txtSal: TextView

    private lateinit var btnBack: ImageView
    private lateinit var btnDeleteEmployee: Button

    // New UI elements for family and holiday leave
    lateinit var txtFamilyStartDate: EditText
    lateinit var txtFamilyEndDate: EditText
    lateinit var txtHolidayStartDate: EditText
    lateinit var txtHolidayEndDate: EditText
    lateinit var btnManLeave: Button


    // New fields for editing
    lateinit var btnEditEmp: Button
    lateinit var btnSaveup: Button
    lateinit var txtnewNum: EditText
    lateinit var txtnewEmail: EditText
    lateinit var txtnewAddress: EditText
    lateinit var txtnewRemainingLeave: EditText
    lateinit var txtnewSal: EditText

    var isEditing = false










    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_employee__info__page, container, false)

        // Retrieve employee info
        val employeeName = arguments?.getString("employeeName")

        btnBack = view.findViewById(R.id.ivBackButton)

        btnBack.setOnClickListener() {
            replaceFragment(EmployeeFragment())
        }

        // Initialize delete button
        btnDeleteEmployee = view.findViewById(R.id.btnDeleteEmp)

        // Initialize leave and holiday UI elements

        btnManLeave = view.findViewById(R.id.btnManLeave)

        //seting up date picker for the textviews

        txtFamilyStartDate.setOnClickListener{
            pickDate(txtFamilyStartDate)
        }
        txtFamilyEndDate.setOnClickListener{
            pickDate(txtFamilyEndDate)
        }


        txtHolidayStartDate.setOnClickListener{
            pickDate(txtHolidayStartDate)
        }
        txtHolidayEndDate.setOnClickListener{
            pickDate(txtHolidayEndDate)
        }



        // Initialize edit and save buttons
        btnEditEmp = view.findViewById(R.id.btnEditEmployeeInfo)
        btnSaveup = view.findViewById(R.id.btnSaveEmployeeInfo)

        // Initialize EditTexts for editing employee info
        txtnewNum = view.findViewById(R.id.txtupdatecell)
        txtnewEmail = view.findViewById(R.id.txtupdateEmail)
        txtnewAddress = view.findViewById(R.id.txtupdateAddress)
        txtnewRemainingLeave = view.findViewById(R.id.txtupdateLeaveRemain)
        txtnewSal = view.findViewById(R.id.txtupdateMonthly)

        btnEditEmp.setOnClickListener {
            toggleEditing(true)
        }

        btnSaveup.setOnClickListener {
            saveEmployeeData(employeeName)
        }


        // Get employee info from DB based on name
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val database = Firebase.database
            val empRef = database.getReference(userId).child("Employees")

            // Query the database to find the employee with the matching name
            val query = empRef.orderByChild("name").equalTo(employeeName)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (projectSnapshot in dataSnapshot.children) {
                        val fetchedEmp = projectSnapshot.getValue(Employee::class.java)
                        if (fetchedEmp != null) {
                            // Assign fetched employee info to text views
                            txtName = view.findViewById(R.id.txtheaderempname)
                            txtName.text = fetchedEmp.name + " " + fetchedEmp.surname
                            txtNum = view.findViewById(R.id.txtempcell_view)
                            txtNum.text = fetchedEmp.number
                            txtEmail = view.findViewById(R.id.txtempEmail_view)
                            txtEmail.text = fetchedEmp.email
                            txtAddress = view.findViewById(R.id.txtempAddress_view)
                            txtAddress.text = fetchedEmp.address
                            txtRemainingLeave = view.findViewById(R.id.txtleave_view)
                            txtRemainingLeave.text = fetchedEmp.leaveLeft
                            txtSal = view.findViewById(R.id.txtmonthly_view)
                            txtSal.text = fetchedEmp.salary

                            // Populate existing leave and holiday dates if available
                            txtFamilyStartDate.setText(fetchedEmp.familyLeaveStart ?: "")
                            txtFamilyEndDate.setText(fetchedEmp.familyLeaveEnd ?: "")
                            txtHolidayStartDate.setText(fetchedEmp.holidayStart ?: "")
                            txtHolidayEndDate.setText(fetchedEmp.holidayEnd ?: "")


                            // Populate EditTexts with employee info for editing
                            txtnewNum.setText(fetchedEmp.number)
                            txtnewEmail.setText(fetchedEmp.email)
                            txtnewAddress.setText(fetchedEmp.address)
                            txtnewRemainingLeave.setText(fetchedEmp.leaveLeft)
                            txtnewSal.setText(fetchedEmp.salary)









                            // Delete employee record
                            btnDeleteEmployee.setOnClickListener {
                                projectSnapshot.ref.removeValue().addOnSuccessListener {
                                    Toast.makeText(
                                        requireContext(),
                                        "Employee deleted successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    replaceFragment(EmployeeFragment())  // Navigate back to employee list
                                }.addOnFailureListener { e ->
                                    Toast.makeText(
                                        requireContext(),
                                        "Failed to delete employee: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }




                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(requireContext(), "Error reading from the database: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        return view
    }

    private fun replaceFragment(fragment: Fragment) {
        Log.d("Employee_Info_Page", "Replacing fragment: ${fragment::class.java.simpleName}")
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }


    fun pickDate(edittxt: TextView)
    {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val datePickDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->

                val formattedDay = String.format("%02d", selectedDay)
                val formattedMonth = String.format("%02d", selectedMonth + 1)

                val selectedDate = "$formattedDay/${formattedMonth}/$selectedYear"
                edittxt.setText(selectedDate)
            }, year, month, day
        )

        datePickDialog.show()
    }



    private fun toggleEditing(editing: Boolean) {
        isEditing = editing

        // Toggle visibility between TextViews and EditTexts
        txtNum.visibility = if (editing) View.GONE else View.VISIBLE
        txtEmail.visibility = if (editing) View.GONE else View.VISIBLE
        txtAddress.visibility = if (editing) View.GONE else View.VISIBLE
        txtRemainingLeave.visibility = if (editing) View.GONE else View.VISIBLE
        txtSal.visibility = if (editing) View.GONE else View.VISIBLE

        txtnewNum.visibility = if (editing) View.VISIBLE else View.GONE
        txtnewEmail.visibility = if (editing) View.VISIBLE else View.GONE
        txtnewAddress.visibility = if (editing) View.VISIBLE else View.GONE
        txtnewRemainingLeave.visibility = if (editing) View.VISIBLE else View.GONE
        txtnewSal.visibility = if (editing) View.VISIBLE else View.GONE

        btnSaveup.visibility = if (editing) View.VISIBLE else View.GONE
        btnEditEmp.visibility = if (!editing) View.VISIBLE else View.GONE
    }




    private fun saveEmployeeData(employeeName: String?) {
        val newNumber = txtnewNum.text.toString()
        val newEmail = txtnewEmail.text.toString()
        val newAddress = txtnewAddress.text.toString()
        val newLeave = txtnewRemainingLeave.text.toString()
        val newSalary = txtnewSal.text.toString()

        if (newNumber.isNotEmpty() && newEmail.isNotEmpty() && newAddress.isNotEmpty() &&
            newLeave.isNotEmpty() && newSalary.isNotEmpty()) {

            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null && employeeName != null) {
                val database = Firebase.database
                val employeeRef = database.getReference(userId).child("Employees").child(employeeName)

                val updates = mapOf<String, Any>(
                    "number" to newNumber,
                    "email" to newEmail,
                    "address" to newAddress,
                    "leaveLeft" to newLeave,
                    "salary" to newSalary
                )

                employeeRef.updateChildren(updates).addOnSuccessListener {
                    Toast.makeText(requireContext(), "Employee updated successfully", Toast.LENGTH_SHORT).show()

                    // Update TextViews with the new data
                    txtNum.text = newNumber
                    txtEmail.text = newEmail
                    txtAddress.text = newAddress
                    txtRemainingLeave.text = newLeave
                    txtSal.text = newSalary

                    // Exit editing mode
                    toggleEditing(false)
                }.addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error updating employee: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show()
        }
    }



}
