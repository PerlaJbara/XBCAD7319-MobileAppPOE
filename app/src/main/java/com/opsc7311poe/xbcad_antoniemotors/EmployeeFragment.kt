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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat


class EmployeeFragment : Fragment() {

    private lateinit var imgPlus: ImageView


    //method to load in each employee
    private fun loadEmployees() {
        /*//fetching projects from database and displaying them in scrollview
        //fetching projects from DB into list
        svAllProjs = findViewById(R.id.svAllProjs)
        linLay = findViewById(R.id.displayEntries)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null)
        {
            val database = Firebase.database

            val projRef = database.getReference(userId).child("projects")

            projRef.addValueEventListener(object: ValueEventListener
            {
                override fun onDataChange(snapshot: DataSnapshot) {
                    linLay.removeAllViews()

                    for(pulledOrder in snapshot.children)
                    {
                        val projName : String? = pulledOrder.child("name").getValue(String::class.java)
                        if (projName != null)
                        {
                            //adding text view with project name
                            val textView = TextView(this@AllProjects)

                            textView.text = projName
                            textView.textSize = 25f
                            textView.height = 120
                            textView.setTextColor(Color.parseColor("#FFFFFF"))
                            textView.typeface = ResourcesCompat.getFont(this@AllProjects, R.font.italiana)
                            //when project name is tapped the project name is logged and the user is taken to project details page
                            textView.setOnClickListener {
                                val intent = Intent(this@AllProjects, ProjectDetails::class.java)
                                intent.putExtra("projectName", textView.text)
                                startActivity(intent)
                            }

                            linLay.addView(textView)
                        }

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(baseContext, "Error reading from the database: " + error.toString(), Toast.LENGTH_SHORT).show()
                }
            })
        }*/

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_employee, container, false)

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