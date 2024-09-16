package com.opsc7311poe.xbcad_antoniemotors

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class Register_Employee_Page : Fragment() {

    private lateinit var btnSubmit : Button
    private lateinit var txtName : TextView
    private lateinit var txtSurname : TextView
    private lateinit var txtSal : TextView
    private lateinit var txtTotalLeave : TextView
    private lateinit var txtLeaveLeft : TextView
    private lateinit var txtNum : TextView
    private lateinit var txtEmail: TextView
    private lateinit var txtAddress: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register__employee__page, container, false)

        btnSubmit = view.findViewById(R.id.btnregEmp)

        btnSubmit.setOnClickListener(){
            //save button
            txtName = view.findViewById(R.id.txtEmpname)
            txtSurname = view.findViewById(R.id.txtEmpsurname)
            txtSal = view.findViewById(R.id.txtEmpsalary)
            txtTotalLeave = view.findViewById(R.id.txtTotal)
            txtLeaveLeft = view.findViewById(R.id.txtleft)
            txtNum = view.findViewById(R.id.txtnumber)
            txtEmail = view.findViewById(R.id.txtEmailInput)
            txtAddress = view.findViewById(R.id.txtAddressInput)


            btnSubmit.setOnClickListener()
            {
                //checking if info is entered correctly
                if(txtName.text.isBlank() || txtSurname.text.isBlank() || txtSal.text.isBlank() || txtTotalLeave.text.isBlank() || txtLeaveLeft.text.isBlank() || txtNum.text.isBlank() || txtEmail.text.isBlank() || txtAddress.text.isBlank())
                {
                    Toast.makeText(requireContext(), "Please enter correct project information.", Toast.LENGTH_SHORT).show()
                }
                else
                {
                    //saving employee information to DB
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null)
                    {
                        var database = Firebase.database
                        var emp = Employee(txtName.text.toString(),txtSurname.text.toString(), txtSal.text.toString(), txtTotalLeave.text.toString(), txtLeaveLeft.text.toString(), txtNum.text.toString(), txtEmail.text.toString(), txtAddress.text.toString())
                        val empRef = database.getReference(userId).child("Employees")

                        empRef.push().setValue(emp)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Employee successfully added", Toast.LENGTH_LONG).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "An error occurred while adding an employee:" + it.toString() , Toast.LENGTH_LONG).show()
                            }
                    }


                    //go back to employee landing page
                    it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    replaceFragment(EmployeeFragment())
                }

            }
        }
        // Inflate the layout for this fragment
        return view
    }


    private fun replaceFragment(fragment: Fragment) {
        Log.d("Register_Employee_Page", "Replacing fragment: ${fragment::class.java.simpleName}")
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}

//employee data class
data class Employee(
    var name: String?,
    var surname: String?,
    var salary: String?,
    var totalLeave: String?,
    var leaveLeft: String?,
    var number: String?,
    var email: String?,
    var address: String?,
    var familyLeaveStart: String? = null,
    var familyLeaveEnd: String? = null,
    var holidayStart: String? = null,
    var holidayEnd: String? = null
){
    // No-argument constructor (required by Firebase)
    constructor() : this(null, null, null, null, null, null, null, null, null,null,null,null,)


}

