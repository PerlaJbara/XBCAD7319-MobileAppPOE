package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView


class AdminEmpFragment : Fragment() {

    private lateinit var imgLeaveman: ImageView
    private lateinit var imgSearchAndReg: ImageView
    private lateinit var imgTask: ImageView
    private lateinit var imganalytic: ImageView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ImageViews
        imgLeaveman = view.findViewById(R.id.imgleavemang)
        imgSearchAndReg = view.findViewById(R.id.imgsearchandreg)
        imgTask = view.findViewById(R.id.imgtask)


        // Set click listeners for the image views

        imgLeaveman.setOnClickListener {
            replaceFragment(AdminLeaveMenuFragment()) // Replace with your actual fragment class
        }

        imgSearchAndReg.setOnClickListener {
            replaceFragment(EmployeeFragment()) // Replace with your actual fragment class
        }

        imgTask.setOnClickListener {
            replaceFragment(AdminTasksMenuFragment()) // Replace with your actual fragment class
        }


        //add fragment
        imganalytic.setOnClickListener {
            replaceFragment(EmployeeFragment()) // Replace with your actual fragment class
        }




    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_admin_emp, container, false)
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
