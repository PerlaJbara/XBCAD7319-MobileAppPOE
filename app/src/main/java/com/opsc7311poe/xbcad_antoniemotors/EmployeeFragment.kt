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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.marginBottom
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database


class EmployeeFragment : Fragment() {

    private lateinit var imgPlus: ImageView
    private lateinit var svEmpList : ScrollView
    private lateinit var linLay : LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_employee, container, false)

        //adding list of employees as buttons
        //fetching employees from DB into list
        svEmpList = view.findViewById(R.id.svEmployeeList)
        linLay = view.findViewById(R.id.linlayEmployees)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null)
        {
            val database = Firebase.database

            val empRef = database.getReference(userId).child("Employees")

            empRef.addValueEventListener(object: ValueEventListener
            {
                override fun onDataChange(snapshot: DataSnapshot) {
                    linLay.removeAllViews()

                    for(pulledOrder in snapshot.children)
                    {
                        val empName : String? = pulledOrder.child("name").getValue(String::class.java)
                        if (empName != null)
                        {
                            //adding text view with project name
                            val TextView = TextView(requireContext())

                            TextView.text = empName
                            TextView.textSize = 25f
                            //TextView.setBackgroundColor(Color.parseColor("#038a39"))
                            TextView.setTextColor(Color.parseColor("#000000"))
                            TextView.typeface = ResourcesCompat.getFont(requireContext(), R.font.fontpoppinsregular)
                            //when Employee name is tapped the project name is logged and the user is taken to project details page
                            /*textView.setOnClickListener {
                                val intent = Intent(this@EmployeeFragment, ProjectDetails::class.java)
                                intent.putExtra("projectName", textView.text)
                                startActivity(intent)
                            }*/

                            linLay.addView(TextView)
                        }

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    //Toast.makeText(baseContext, "Error reading from the database: " + error.toString(), Toast.LENGTH_SHORT).show()
                }
            })
        }


        //plus button functionality
        imgPlus = view.findViewById(R.id.imgPlus)

        imgPlus.setOnClickListener(){
            it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            replaceFragment(Register_Employee_Page())
        }

        return view
    }

    private fun replaceFragment(fragment: Fragment) {
        Log.d("EmployeeFragment", "Replacing fragment: ${fragment::class.java.simpleName}")
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }
}