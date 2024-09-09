package com.opsc7311poe.xbcad_antoniemotors

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class EmployeeFragment : Fragment() {

    private lateinit var imgPlus: ImageView
    private lateinit var svEmpList: ScrollView
    private lateinit var linLay: LinearLayout
    private lateinit var searchBar: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_employee, container, false)

        // Adding search bar
        searchBar = view.findViewById(R.id.txtEmpSearched)

        // ScrollView and LinearLayout setup for employee list
        svEmpList = view.findViewById(R.id.svEmployeeList)
        linLay = view.findViewById(R.id.linlayEmployees)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val database = Firebase.database
            val empRef = database.getReference(userId).child("Employees")

            empRef.addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    linLay.removeAllViews()

                    for (pulledOrder in snapshot.children) {
                        val empName: String? = pulledOrder.child("name").getValue(String::class.java)
                        if (empName != null) {

                            val employeeButton = Button(requireContext())


                            employeeButton.text = empName
                            employeeButton.textSize = 20f
                            employeeButton.setBackgroundColor(Color.parseColor("#038a39"))
                            employeeButton.setTextColor(Color.WHITE)
                            employeeButton.typeface = ResourcesCompat.getFont(requireContext(), R.font.fontpoppinsregular)
                            employeeButton.setPadding(20, 20, 20, 20)
                            employeeButton.background = ResourcesCompat.getDrawable(resources, R.drawable.gbutton_round, null)

                            val layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            layoutParams.setMargins(0, 20, 0, 20) // Add margins for spacing
                            employeeButton.layoutParams = layoutParams

                            //when Employee name is tapped the project name is logged and the user is taken to project details page
                            employeeButton.setOnClickListener {

                                val employeeInfoFragment = Employee_Info_Page()
                                //transferring employee info using a bundle
                                val bundle = Bundle()
                                bundle.putString("employeeName", empName)
                                employeeInfoFragment.arguments = bundle
                                //changing to employee info fragment
                                it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                replaceFragment(employeeInfoFragment)
                            }

                            // Add the button to the LinearLayout
                            linLay.addView(employeeButton)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error here
                }
            })
        }

        // Plus button functionality
        imgPlus = view.findViewById(R.id.imgPlus)
        imgPlus.setOnClickListener() {
            it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            replaceFragment(Register_Employee_Page())
        }

        return view
    }

    private fun replaceFragment(fragment: Fragment) {
        Log.d("EmployeeFragment", "Replacing fragment: ${fragment::class.java.simpleName}")
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
