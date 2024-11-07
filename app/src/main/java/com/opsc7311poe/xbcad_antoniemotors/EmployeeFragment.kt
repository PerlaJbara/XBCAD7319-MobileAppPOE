package com.opsc7311poe.xbcad_antoniemotors

import android.content.Context
import android.graphics.Color
import android.os.Bundle
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
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class EmployeeFragment : Fragment() {

    private lateinit var svEmpList: ScrollView
    private lateinit var linLay: LinearLayout
    private lateinit var searchBar: EditText
    private lateinit var btnSearch: ImageView

    private lateinit var businessId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_employee, container, false)

        businessId = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).getString("business_id", null)!!
        searchBar = view.findViewById(R.id.txtEmpSearched)
        btnSearch = view.findViewById(R.id.searchimage)

        btnSearch.setOnClickListener {
            findEmployee()
        }

        svEmpList = view.findViewById(R.id.svEmployeeList)
        linLay = view.findViewById(R.id.linlayEmployees)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val database = Firebase.database
            val empRef = database.getReference("Users").child(businessId).child("Employees")

            empRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    linLay.removeAllViews()

                    for (pulledOrder in snapshot.children) {
                        val managerId: String? = pulledOrder.child("managerID").getValue(String::class.java)
                        val fName: String? = pulledOrder.child("firstName").getValue(String::class.java)
                        val lName: String? = pulledOrder.child("lastName").getValue(String::class.java)

                        // Check if managerId matches the logged-in user ID
                        if (managerId == userId) {
                            val empName = "$fName $lName"

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
                            layoutParams.setMargins(0, 20, 0, 20)
                            employeeButton.layoutParams = layoutParams

                            employeeButton.setOnClickListener {
                                val employeeInfoFragment = Employee_Info_Page()
                                val bundle = Bundle()

                                // Pass employeeId instead of employeeName
                                val employeeId = pulledOrder.key
                                bundle.putString("employeeId", employeeId)

                                employeeInfoFragment.arguments = bundle
                                it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                replaceFragment(employeeInfoFragment)
                            }

                            linLay.addView(employeeButton)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error fetching employees: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        return view
    }

    private fun findEmployee() {
        val query = searchBar.text.toString().trim().lowercase()

        if (query.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a name to search", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val database = Firebase.database
        val empRef = database.getReference("Users").child(businessId).child("Employees")

        empRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                linLay.removeAllViews()

                for (pulledOrder in snapshot.children) {
                    val managerId: String? = pulledOrder.child("managerID").getValue(String::class.java)
                    val fName: String? = pulledOrder.child("firstName").getValue(String::class.java)
                    val lName: String? = pulledOrder.child("lastName").getValue(String::class.java)

                    val empName = "$fName $lName".trim()

                    if (managerId == userId && empName.lowercase().contains(query)) {
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
                        layoutParams.setMargins(0, 20, 0, 20)
                        employeeButton.layoutParams = layoutParams

                        employeeButton.setOnClickListener {
                            val employeeInfoFragment = Employee_Info_Page()
                            val bundle = Bundle()
                            bundle.putString("employeeName", empName)
                            employeeInfoFragment.arguments = bundle
                            it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            replaceFragment(employeeInfoFragment)
                        }

                        linLay.addView(employeeButton)
                    }
                }

                if (linLay.childCount == 0) {
                    Toast.makeText(requireContext(), "No employees found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error fetching employees: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
