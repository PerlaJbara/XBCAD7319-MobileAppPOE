package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class Employee_Info_Page : Fragment() {

    lateinit var txtName : TextView
    lateinit var txtNum : TextView
    lateinit var txtEmail : TextView
    lateinit var txtAddress: TextView
    lateinit var txtRemainingLeave: TextView
    lateinit var txtSal: TextView

    private lateinit var btnBack: ImageView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_employee__info__page, container, false)

        //retrieve employee info
        val employeeName = arguments?.getString("employeeName")

        btnBack = view.findViewById(R.id.ivBackButton)

        btnBack.setOnClickListener(){
            replaceFragment(EmployeeFragment())
        }
        //get rest of employee info based on name from DB
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null)
        {
            val database = Firebase.database
            val empRef = database.getReference(userId).child("Employees")

            // Query the database to find the project with the matching name
            val query = empRef.orderByChild("name").equalTo(employeeName)
            query.addListenerForSingleValueEvent(object : ValueEventListener
            {
                override fun onDataChange(dataSnapshot: DataSnapshot)
                {
                    for (projectSnapshot in dataSnapshot.children)
                    {
                        val fetchedEmp = projectSnapshot.getValue(Employee::class.java)
                        if (fetchedEmp != null) {
                            //assigning fetched employee info to text views
                            txtName = view.findViewById(R.id.txtheaderempname)
                            txtName.text = fetchedEmp.name
                            txtNum = view.findViewById(R.id.txtempcell)
                            txtNum.text = fetchedEmp.number
                            txtEmail = view.findViewById(R.id.txtempEmail)
                            txtEmail.text = fetchedEmp.email
                            txtAddress = view.findViewById(R.id.txtempAddress)
                            txtAddress.text = fetchedEmp.address
                            txtRemainingLeave = view.findViewById(R.id.txtleave)
                            txtRemainingLeave.text = fetchedEmp.leaveLeft
                            txtSal = view.findViewById(R.id.txtmonthly)
                            txtSal.text = fetchedEmp.salary
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError)
                {
                    Toast.makeText(requireContext(), "Error reading from the database: " + databaseError.toString(), Toast.LENGTH_SHORT).show()
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
}